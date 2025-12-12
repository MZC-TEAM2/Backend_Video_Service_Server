package com.teambind.springproject.domain.upload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 비디오 업로드 엔티티.
 */
@Entity
@Table(
    name = "video_upload",
    indexes = {
        @Index(name = "idx_upload_user", columnList = "user_id"),
        @Index(name = "idx_upload_status", columnList = "status"),
        @Index(name = "idx_upload_tus_id", columnList = "tus_upload_id")
    }
)
public class VideoUpload {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tus_upload_id", nullable = false, unique = true)
  private String tusUploadId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "uploaded_bytes", nullable = false)
  private Long uploadedBytes = 0L;

  @Column(name = "content_type")
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UploadStatus status = UploadStatus.IN_PROGRESS;

  @Column(name = "storage_path")
  private String storagePath;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  protected VideoUpload() {
  }

  private VideoUpload(
      final String tusUploadId,
      final Long userId,
      final String originalFilename,
      final Long fileSize,
      final String contentType
  ) {
    this.tusUploadId = tusUploadId;
    this.userId = userId;
    this.originalFilename = originalFilename;
    this.fileSize = fileSize;
    this.contentType = contentType;
    this.uploadedBytes = 0L;
    this.status = UploadStatus.IN_PROGRESS;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 새로운 업로드를 생성한다.
   *
   * @param tusUploadId TUS 업로드 ID
   * @param userId 사용자 ID
   * @param originalFilename 원본 파일명
   * @param fileSize 파일 크기
   * @param contentType 콘텐츠 타입
   * @return VideoUpload 인스턴스
   */
  public static VideoUpload create(
      final String tusUploadId,
      final Long userId,
      final String originalFilename,
      final Long fileSize,
      final String contentType
  ) {
    return new VideoUpload(tusUploadId, userId, originalFilename, fileSize, contentType);
  }

  /**
   * 업로드 진행 상황을 업데이트한다.
   *
   * @param uploadedBytes 업로드된 바이트 수
   */
  public void updateProgress(final Long uploadedBytes) {
    this.uploadedBytes = uploadedBytes;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 업로드를 완료 처리한다.
   *
   * @param storagePath 저장 경로
   */
  public void complete(final String storagePath) {
    this.status = UploadStatus.COMPLETED;
    this.storagePath = storagePath;
    this.uploadedBytes = this.fileSize;
    this.completedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 업로드를 취소한다.
   */
  public void cancel() {
    this.status = UploadStatus.CANCELLED;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 업로드를 실패 처리한다.
   */
  public void fail() {
    this.status = UploadStatus.FAILED;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 업로드 진행률을 계산한다.
   *
   * @return 진행률 (0-100)
   */
  public int getProgressPercentage() {
    if (fileSize == 0) {
      return 0;
    }
    return (int) ((uploadedBytes * 100) / fileSize);
  }

  /**
   * 업로드가 진행 중인지 확인한다.
   *
   * @return 진행 중이면 true
   */
  public boolean isInProgress() {
    return status == UploadStatus.IN_PROGRESS;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getTusUploadId() {
    return tusUploadId;
  }

  public Long getUserId() {
    return userId;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public Long getUploadedBytes() {
    return uploadedBytes;
  }

  public String getContentType() {
    return contentType;
  }

  public UploadStatus getStatus() {
    return status;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }
}
