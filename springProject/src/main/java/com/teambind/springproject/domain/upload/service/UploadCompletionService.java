package com.teambind.springproject.domain.upload.service;

import com.teambind.springproject.domain.upload.entity.VideoUpload;
import com.teambind.springproject.domain.upload.repository.VideoUploadRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.upload.UploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업로드 완료 처리 서비스.
 */
@Service
public class UploadCompletionService {

  private static final Logger log = LoggerFactory.getLogger(UploadCompletionService.class);

  private final TusFileUploadService tusService;
  private final VideoUploadRepository uploadRepository;
  private final String storagePath;

  public UploadCompletionService(
      final TusFileUploadService tusService,
      final VideoUploadRepository uploadRepository,
      @Value("${video.upload.storage-path:/tmp/video-uploads}") final String storagePath
  ) {
    this.tusService = tusService;
    this.uploadRepository = uploadRepository;
    this.storagePath = storagePath;
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
   * 업로드 메타데이터를 저장한다.
   *
   * @param uploadUri TUS 업로드 URI
   * @param userId 사용자 ID
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

    VideoUpload upload = VideoUpload.create(
        tusUploadId,
        userId,
        filename,
        uploadInfo.getLength(),
        uploadInfo.getFileMimeType()
    );

    uploadRepository.save(upload);
    log.debug("업로드 메타데이터 저장: tusUploadId={}, filename={}", tusUploadId, filename);
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

  private void updateUploadRecord(final String tusUploadId, final String storagePath) {
    uploadRepository.findByTusUploadId(tusUploadId)
        .ifPresent(upload -> {
          upload.complete(storagePath);
          uploadRepository.save(upload);
        });
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
