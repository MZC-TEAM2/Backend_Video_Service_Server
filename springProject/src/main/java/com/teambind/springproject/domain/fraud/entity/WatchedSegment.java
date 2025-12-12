package com.teambind.springproject.domain.fraud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 시청 구간 엔티티.
 * 사용자가 실제로 시청한 구간을 기록한다.
 */
@Entity
@Table(
    name = "watched_segment",
    indexes = {
        @Index(name = "idx_segment_session", columnList = "session_id"),
        @Index(name = "idx_segment_user_content", columnList = "user_id, content_id")
    }
)
public class WatchedSegment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "content_id", nullable = false)
  private Long contentId;

  @Column(name = "start_seconds", nullable = false)
  private Integer startSeconds;

  @Column(name = "end_seconds", nullable = false)
  private Integer endSeconds;

  @Column(name = "is_valid", nullable = false)
  private Boolean isValid = true;

  @Column(name = "playback_rate")
  private Double playbackRate = 1.0;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected WatchedSegment() {
  }

  private WatchedSegment(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final Integer startSeconds,
      final Integer endSeconds,
      final Boolean isValid,
      final Double playbackRate
  ) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.contentId = contentId;
    this.startSeconds = startSeconds;
    this.endSeconds = endSeconds;
    this.isValid = isValid;
    this.playbackRate = playbackRate;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * 유효한 시청 구간을 생성한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param startSeconds 시작 시간(초)
   * @param endSeconds 종료 시간(초)
   * @param playbackRate 재생 속도
   * @return WatchedSegment 인스턴스
   */
  public static WatchedSegment createValid(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final Integer startSeconds,
      final Integer endSeconds,
      final Double playbackRate
  ) {
    boolean isValid = playbackRate == null || playbackRate <= 1.0;
    return new WatchedSegment(
        sessionId, userId, contentId, startSeconds, endSeconds, isValid, playbackRate
    );
  }

  /**
   * 무효한 시청 구간을 생성한다. (스킵, 탭 전환 등)
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param startSeconds 시작 시간(초)
   * @param endSeconds 종료 시간(초)
   * @return WatchedSegment 인스턴스
   */
  public static WatchedSegment createInvalid(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final Integer startSeconds,
      final Integer endSeconds
  ) {
    return new WatchedSegment(
        sessionId, userId, contentId, startSeconds, endSeconds, false, null
    );
  }

  /**
   * 구간의 길이를 반환한다.
   *
   * @return 구간 길이(초)
   */
  public int getDuration() {
    return endSeconds - startSeconds;
  }

  /**
   * 구간을 무효화한다.
   */
  public void invalidate() {
    this.isValid = false;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getContentId() {
    return contentId;
  }

  public Integer getStartSeconds() {
    return startSeconds;
  }

  public Integer getEndSeconds() {
    return endSeconds;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public Double getPlaybackRate() {
    return playbackRate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
