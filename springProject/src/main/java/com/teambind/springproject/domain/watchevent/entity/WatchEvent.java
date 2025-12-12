package com.teambind.springproject.domain.watchevent.entity;

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
 * 시청 이벤트 엔티티.
 * 클라이언트에서 발생하는 모든 시청 관련 이벤트를 기록한다.
 */
@Entity
@Table(
    name = "watch_event",
    indexes = {
        @Index(name = "idx_watch_event_session", columnList = "session_id"),
        @Index(name = "idx_watch_event_user", columnList = "user_id"),
        @Index(name = "idx_watch_event_content", columnList = "content_id"),
        @Index(name = "idx_watch_event_type", columnList = "event_type"),
        @Index(name = "idx_watch_event_timestamp", columnList = "event_timestamp")
    }
)
public class WatchEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "content_id", nullable = false)
  private Long contentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 30)
  private WatchEventType eventType;

  @Column(name = "event_timestamp", nullable = false)
  private LocalDateTime eventTimestamp;

  @Column(name = "position_seconds")
  private Integer positionSeconds;

  @Column(name = "from_position_seconds")
  private Integer fromPositionSeconds;

  @Column(name = "to_position_seconds")
  private Integer toPositionSeconds;

  @Column(name = "playback_rate")
  private Double playbackRate;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected WatchEvent() {
  }

  private WatchEvent(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final WatchEventType eventType,
      final LocalDateTime eventTimestamp,
      final Integer positionSeconds,
      final Integer fromPositionSeconds,
      final Integer toPositionSeconds,
      final Double playbackRate
  ) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.contentId = contentId;
    this.eventType = eventType;
    this.eventTimestamp = eventTimestamp;
    this.positionSeconds = positionSeconds;
    this.fromPositionSeconds = fromPositionSeconds;
    this.toPositionSeconds = toPositionSeconds;
    this.playbackRate = playbackRate;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * 재생/일시정지 이벤트를 생성한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param eventType 이벤트 타입 (PLAY, PAUSE)
   * @param eventTimestamp 이벤트 발생 시간
   * @param positionSeconds 현재 재생 위치
   * @return WatchEvent 인스턴스
   */
  public static WatchEvent createPlaybackEvent(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final WatchEventType eventType,
      final LocalDateTime eventTimestamp,
      final Integer positionSeconds
  ) {
    return new WatchEvent(
        sessionId, userId, contentId, eventType, eventTimestamp,
        positionSeconds, null, null, null
    );
  }

  /**
   * 구간 이동(SEEK) 이벤트를 생성한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param eventTimestamp 이벤트 발생 시간
   * @param fromPositionSeconds 이동 전 위치
   * @param toPositionSeconds 이동 후 위치
   * @return WatchEvent 인스턴스
   */
  public static WatchEvent createSeekEvent(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final LocalDateTime eventTimestamp,
      final Integer fromPositionSeconds,
      final Integer toPositionSeconds
  ) {
    return new WatchEvent(
        sessionId, userId, contentId, WatchEventType.SEEK, eventTimestamp,
        null, fromPositionSeconds, toPositionSeconds, null
    );
  }

  /**
   * 재생 속도 변경 이벤트를 생성한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param eventTimestamp 이벤트 발생 시간
   * @param playbackRate 변경된 재생 속도
   * @return WatchEvent 인스턴스
   */
  public static WatchEvent createRateChangeEvent(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final LocalDateTime eventTimestamp,
      final Double playbackRate
  ) {
    return new WatchEvent(
        sessionId, userId, contentId, WatchEventType.RATE_CHANGE, eventTimestamp,
        null, null, null, playbackRate
    );
  }

  /**
   * 가시성 변경 이벤트를 생성한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param eventType 이벤트 타입 (VISIBILITY_HIDDEN, VISIBILITY_VISIBLE)
   * @param eventTimestamp 이벤트 발생 시간
   * @return WatchEvent 인스턴스
   */
  public static WatchEvent createVisibilityEvent(
      final Long sessionId,
      final Long userId,
      final Long contentId,
      final WatchEventType eventType,
      final LocalDateTime eventTimestamp
  ) {
    return new WatchEvent(
        sessionId, userId, contentId, eventType, eventTimestamp,
        null, null, null, null
    );
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

  public WatchEventType getEventType() {
    return eventType;
  }

  public LocalDateTime getEventTimestamp() {
    return eventTimestamp;
  }

  public Integer getPositionSeconds() {
    return positionSeconds;
  }

  public Integer getFromPositionSeconds() {
    return fromPositionSeconds;
  }

  public Integer getToPositionSeconds() {
    return toPositionSeconds;
  }

  public Double getPlaybackRate() {
    return playbackRate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
