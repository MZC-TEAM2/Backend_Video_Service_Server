package com.teambind.springproject.domain.progress.dto;

import com.teambind.springproject.domain.progress.entity.StudentContentProgress;
import java.time.LocalDateTime;

/**
 * 시청 진행 상황 응답 DTO.
 */
public record ProgressResponse(
    Long id,
    Long contentId,
    Long studentId,
    Boolean isCompleted,
    Integer progressPercentage,
    Integer lastPositionSeconds,
    LocalDateTime completedAt,
    LocalDateTime firstAccessedAt,
    LocalDateTime lastAccessedAt,
    Integer accessCount
) {

  /**
   * StudentContentProgress 엔티티로부터 응답 DTO를 생성한다.
   *
   * @param progress 학생 콘텐츠 진행 상황
   * @return ProgressResponse
   */
  public static ProgressResponse from(final StudentContentProgress progress) {
    return new ProgressResponse(
        progress.getId(),
        progress.getContentId(),
        progress.getStudentId(),
        progress.getIsCompleted(),
        progress.getProgressPercentage(),
        progress.getLastPositionSeconds(),
        progress.getCompletedAt(),
        progress.getFirstAccessedAt(),
        progress.getLastAccessedAt(),
        progress.getAccessCount()
    );
  }
}
