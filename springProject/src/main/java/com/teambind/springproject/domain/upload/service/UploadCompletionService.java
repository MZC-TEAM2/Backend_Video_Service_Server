package com.teambind.springproject.domain.upload.service;

import com.teambind.springproject.domain.content.entity.WeekContent;
import com.teambind.springproject.domain.content.repository.WeekContentRepository;
import com.teambind.springproject.domain.upload.entity.VideoUpload;
import com.teambind.springproject.domain.upload.repository.VideoUploadRepository;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.upload.UploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * 업로드 완료 처리 서비스.
 */
@Service
public class UploadCompletionService {
	
	private static final Logger log = LoggerFactory.getLogger(UploadCompletionService.class);
	
	private final TusFileUploadService tusService;
	private final VideoUploadRepository uploadRepository;
	private final WeekContentRepository weekContentRepository;
	private final String storagePath;
	private final String baseUrl;
	
	public UploadCompletionService(
			final TusFileUploadService tusService,
			final VideoUploadRepository uploadRepository,
			final WeekContentRepository weekContentRepository,
			@Value("${video.upload.storage-path:/tmp/video-uploads}") final String storagePath,
			@Value("${video.stream.base-url:}") final String baseUrl
	) {
		this.tusService = tusService;
		this.uploadRepository = uploadRepository;
		this.weekContentRepository = weekContentRepository;
		this.storagePath = storagePath;
		this.baseUrl = baseUrl;
	}
	
	/**
	 * 업로드 완료를 처리한다.
	 *
	 * @param uploadUri TUS 업로드 URI
	 * @return 저장된 파일 경로
	 */
	@Transactional
	public String completeUpload(final String uploadUri) throws Exception {
		UploadInfo uploadInfo = tusService.getUploadInfo(uploadUri);
		
		if (uploadInfo == null) {
			throw new IllegalArgumentException("업로드 정보를 찾을 수 없습니다: " + uploadUri);
		}
		
		if (!uploadInfo.isUploadInProgress() && uploadInfo.getOffset().equals(uploadInfo.getLength())) {
			// 업로드 완료됨 - 파일 이동
			String finalPath = moveToStorage(uploadUri, uploadInfo);
			
			// DB 업데이트
			String tusUploadId = extractUploadId(uploadUri);
			VideoUpload upload = updateUploadRecord(tusUploadId, finalPath);
			
			// week_contents 테이블에 등록
			if (upload != null && upload.getWeekId() != null) {
				createWeekContent(upload);
			}
			
			// TUS 임시 파일 정리
			tusService.deleteUpload(uploadUri);
			
			log.info("업로드 완료: uploadUri={}, storagePath={}", uploadUri, finalPath);
			return finalPath;
		}
		
		throw new IllegalStateException("업로드가 아직 완료되지 않았습니다.");
	}
	
	/**
	 * 업로드 메타데이터를 저장한다.
	 *
	 * @param uploadUri TUS 업로드 URI
	 * @param userId    사용자 ID
	 */
	@Transactional
	public void saveUploadMetadata(final String uploadUri, final Long userId) throws Exception {
		UploadInfo uploadInfo = tusService.getUploadInfo(uploadUri);
		
		if (uploadInfo == null) {
			throw new IllegalArgumentException("업로드 정보를 찾을 수 없습니다: " + uploadUri);
		}
		
		String tusUploadId = extractUploadId(uploadUri);
		String filename = uploadInfo.getFileName();
		if (filename == null || filename.isEmpty()) {
			filename = "unknown";
		}
		
		// TUS 메타데이터에서 courseId, weekId, title, duration 추출
		Map<String, String> metadata = uploadInfo.getMetadata();
		Long courseId = extractLongMetadata(metadata, "courseId");
		Long weekId = extractLongMetadata(metadata, "weekId");
		String title = extractStringMetadata(metadata, "title", filename);
		String duration = extractStringMetadata(metadata, "duration", null);
		
		VideoUpload upload = VideoUpload.create(
				tusUploadId,
				userId,
				filename,
				uploadInfo.getLength(),
				uploadInfo.getFileMimeType(),
				courseId,
				weekId,
				title
		);
		if (duration != null) {
			upload.setDuration(duration);
		}
		
		uploadRepository.save(upload);
		log.debug("업로드 메타데이터 저장: tusUploadId={}, filename={}, courseId={}, weekId={}, title={}, duration={}",
				tusUploadId, filename, courseId, weekId, title, duration);
	}
	
	/**
	 * 업로드 진행 상황을 업데이트한다.
	 *
	 * @param uploadUri TUS 업로드 URI
	 */
	@Transactional
	public void updateUploadProgress(final String uploadUri) throws Exception {
		UploadInfo uploadInfo = tusService.getUploadInfo(uploadUri);
		
		if (uploadInfo == null) {
			return;
		}
		
		String tusUploadId = extractUploadId(uploadUri);
		uploadRepository.findByTusUploadId(tusUploadId)
				.ifPresent(upload -> {
					upload.updateProgress(uploadInfo.getOffset());
					uploadRepository.save(upload);
				});
	}
	
	private String moveToStorage(final String uploadUri, final UploadInfo uploadInfo)
			throws Exception {
		// 저장 디렉토리 생성
		Path storageDir = Paths.get(storagePath);
		Files.createDirectories(storageDir);
		
		// UUID 기반 파일명 생성
		String originalFilename = uploadInfo.getFileName();
		String extension = getFileExtension(originalFilename);
		String newFilename = UUID.randomUUID().toString() + extension;
		Path targetPath = storageDir.resolve(newFilename);
		
		// 파일 복사
		try (InputStream inputStream = tusService.getUploadedBytes(uploadUri)) {
			Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}
		
		return targetPath.toString();
	}
	
	private VideoUpload updateUploadRecord(final String tusUploadId, final String storagePath) {
		return uploadRepository.findByTusUploadId(tusUploadId)
				.map(upload -> {
					upload.complete(storagePath);
					return uploadRepository.save(upload);
				})
				.orElse(null);
	}
	
	private void createWeekContent(final VideoUpload upload) {
		// 표시 순서 계산
		Integer maxOrder = weekContentRepository.findMaxDisplayOrderByWeekId(upload.getWeekId());
		Integer displayOrder = maxOrder + 1;

		// WeekContent 생성 및 저장 (임시 URL로 먼저 저장)
		WeekContent weekContent = WeekContent.createVideo(
				upload.getCourseId(),
				upload.getWeekId(),
				upload.getTitle(),
				"",  // 임시 빈 URL
				upload.getDuration(),
				displayOrder
		);

		// 저장하여 contentId 생성
		WeekContent savedContent = weekContentRepository.save(weekContent);

		// 실제 시청 URL 생성 (contentId, videoId 포함)
		String watchUrl = buildWatchUrl(savedContent.getId(), upload.getId());

		// URL 업데이트
		savedContent.updateContentUrl(watchUrl);
		weekContentRepository.save(savedContent);

		log.info("WeekContent 생성: courseId={}, weekId={}, title={}, watchUrl={}",
				upload.getCourseId(), upload.getWeekId(), upload.getTitle(), watchUrl);
	}

	private String buildWatchUrl(final Long contentId, final Long videoId) {
		String watchPath = "/watch/" + contentId + "/" + videoId;
		if (baseUrl != null && !baseUrl.isEmpty()) {
			return baseUrl + watchPath;
		}
		return watchPath;
	}
	
	private Long extractLongMetadata(final Map<String, String> metadata, final String key) {
		if (metadata == null || !metadata.containsKey(key)) {
			return null;
		}
		try {
			return Long.parseLong(metadata.get(key));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private String extractStringMetadata(
			final Map<String, String> metadata,
			final String key,
			final String defaultValue
	) {
		if (metadata == null || !metadata.containsKey(key)) {
			return defaultValue;
		}
		String value = metadata.get(key);
		return (value != null && !value.isEmpty()) ? value : defaultValue;
	}
	
	private String extractUploadId(final String uploadUri) {
		// /api/v1/videos/upload/xxx 에서 xxx 추출
		int lastSlash = uploadUri.lastIndexOf('/');
		if (lastSlash >= 0 && lastSlash < uploadUri.length() - 1) {
			return uploadUri.substring(lastSlash + 1);
		}
		return uploadUri;
	}
	
	private String getFileExtension(final String filename) {
		if (filename == null || filename.isEmpty()) {
			return "";
		}
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0) {
			return filename.substring(dotIndex);
		}
		return "";
	}
}
