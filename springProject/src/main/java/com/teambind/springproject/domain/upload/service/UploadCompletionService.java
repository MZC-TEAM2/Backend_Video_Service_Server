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
			@Value("${video.stream.base-url:http://localhost:8090}") final String baseUrl
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
			updateUploadRecord(tusUploadId, finalPath);

			// TUS 임시 파일 정리
			tusService.deleteUpload(uploadUri);

			log.info("업로드 완료: uploadUri={}, storagePath={}", uploadUri, finalPath);
			return finalPath;
		}

		throw new IllegalStateException("업로드가 아직 완료되지 않았습니다.");
	}

	/**
	 * 업로드 메타데이터를 저장하고 week_contents를 생성한다.
	 * 업로드 시작 시점에 호출되어 watchUrl을 반환한다.
	 *
	 * @param uploadUri TUS 업로드 URI
	 * @param userId    사용자 ID
	 * @return UploadResult (videoId, contentId, watchUrl 포함)
	 */
	@Transactional
	public UploadResult saveUploadMetadataAndCreateContent(final String uploadUri, final Long userId) throws Exception {
		UploadInfo uploadInfo = tusService.getUploadInfo(uploadUri);

		if (uploadInfo == null) {
			throw new IllegalArgumentException("업로드 정보를 찾을 수 없습니다: " + uploadUri);
		}

		String tusUploadId = extractUploadId(uploadUri);
		String filename = uploadInfo.getFileName();
		if (filename == null || filename.isEmpty()) {
			filename = "unknown";
		}

		// TUS 메타데이터에서 weekId, title, duration 추출
		Map<String, String> metadata = uploadInfo.getMetadata();
		Long weekId = extractLongMetadata(metadata, "weekId");
		String title = extractStringMetadata(metadata, "title", filename);
		String duration = extractStringMetadata(metadata, "duration", null);

		// VideoUpload 생성
		VideoUpload upload = VideoUpload.create(
				tusUploadId,
				userId,
				filename,
				uploadInfo.getLength(),
				uploadInfo.getFileMimeType(),
				weekId,
				title
		);
		if (duration != null) {
			upload.setDuration(duration);
		}

		// VideoUpload 저장
		VideoUpload savedUpload = uploadRepository.save(upload);

		// week_contents 생성 (weekId가 있을 경우)
		Long contentId = null;
		String watchUrl = null;

		if (weekId != null) {
			// 표시 순서 계산
			Integer maxOrder = weekContentRepository.findMaxDisplayOrderByWeekId(weekId);
			Integer displayOrder = (maxOrder != null ? maxOrder : 0) + 1;

			// WeekContent 생성 (임시 URL로 먼저 저장)
			WeekContent weekContent = WeekContent.createVideo(
					weekId,
					title,
					"",  // 임시 빈 URL
					duration,
					displayOrder
			);

			// 저장하여 contentId 생성
			WeekContent savedContent = weekContentRepository.save(weekContent);
			contentId = savedContent.getId();

			// 실제 시청 URL 생성
			watchUrl = buildWatchUrl(contentId, savedUpload.getId());

			// URL 업데이트
			savedContent.updateContentUrl(watchUrl);
			weekContentRepository.save(savedContent);

			// VideoUpload에 contentId 저장
			savedUpload.setContentId(contentId);
			uploadRepository.save(savedUpload);

			log.info("업로드 시작 - WeekContent 생성: weekId={}, title={}, watchUrl={}",
					weekId, title, watchUrl);
		}

		log.debug("업로드 메타데이터 저장: tusUploadId={}, filename={}, weekId={}, title={}, duration={}",
				tusUploadId, filename, weekId, title, duration);

		return new UploadResult(savedUpload.getId(), contentId, watchUrl);
	}

	/**
	 * PATCH 요청 완료 후 처리.
	 * 업로드 완료 여부를 확인하고 완료 시 파일을 저장소로 이동한다.
	 *
	 * @param uploadUri TUS 업로드 URI
	 */
	@Transactional
	public void handlePatchCompletion(final String uploadUri) throws Exception {
		UploadInfo uploadInfo = tusService.getUploadInfo(uploadUri);

		if (uploadInfo == null) {
			return;
		}

		// 업로드 완료 여부 확인
		if (!uploadInfo.isUploadInProgress()
				&& uploadInfo.getOffset().equals(uploadInfo.getLength())) {
			log.info("업로드 완료 감지: uri={}, size={}", uploadUri, uploadInfo.getLength());

			try {
				String storagePath = completeUpload(uploadUri);
				log.info("파일 저장 완료: {}", storagePath);
			} catch (Exception e) {
				log.error("업로드 완료 처리 실패: {}", e.getMessage(), e);
			}
		} else {
			// 진행 상황 업데이트
			updateUploadProgress(uploadUri);
		}
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

	/**
	 * 업로드 결과를 담는 DTO.
	 */
	public static class UploadResult {
		private final Long videoId;
		private final Long contentId;
		private final String watchUrl;

		public UploadResult(final Long videoId, final Long contentId, final String watchUrl) {
			this.videoId = videoId;
			this.contentId = contentId;
			this.watchUrl = watchUrl;
		}

		public Long getVideoId() {
			return videoId;
		}

		public Long getContentId() {
			return contentId;
		}

		public String getWatchUrl() {
			return watchUrl;
		}
	}
}
