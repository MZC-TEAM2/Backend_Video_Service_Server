package com.teambind.springproject.domain.session.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 동영상 시청 세션을 나타내는 도메인 객체.
 * Redis에 저장되어 활성 세션을 관리한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WatchSession implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Long sessionId;
	private final Long userId;
	private final Long contentId;
	private final LocalDateTime startedAt;
	private LocalDateTime lastActiveAt;
	private WatchSessionStatus status;
	private Integer lastPositionSeconds;
	
	@JsonCreator
	public WatchSession(
			@JsonProperty("sessionId") final Long sessionId,
			@JsonProperty("userId") final Long userId,
			@JsonProperty("contentId") final Long contentId,
			@JsonProperty("startedAt") final LocalDateTime startedAt,
			@JsonProperty("lastActiveAt") final LocalDateTime lastActiveAt,
			@JsonProperty("status") final WatchSessionStatus status,
			@JsonProperty("lastPositionSeconds") final Integer lastPositionSeconds
	) {
		this.sessionId = sessionId;
		this.userId = userId;
		this.contentId = contentId;
		this.startedAt = startedAt;
		this.lastActiveAt = lastActiveAt;
		this.status = status;
		this.lastPositionSeconds = lastPositionSeconds;
	}
	
	/**
	 * 새로운 시청 세션을 생성한다.
	 *
	 * @param sessionId 세션 ID (Snowflake로 생성)
	 * @param userId    사용자 ID
	 * @param contentId 콘텐츠 ID
	 * @return 새로운 WatchSession 인스턴스
	 */
	public static WatchSession create(
			final Long sessionId,
			final Long userId,
			final Long contentId
	) {
		LocalDateTime now = LocalDateTime.now();
		return new WatchSession(
				sessionId,
				userId,
				contentId,
				now,
				now,
				WatchSessionStatus.ACTIVE,
				0
		);
	}
	
	
	/**
	 * 시청 진행 상황을 업데이트한다.
	 *
	 * @param positionSeconds 현재 시청 위치 (초)
	 */
	public void updateProgress(final Integer positionSeconds) {
		validateActiveSession();
		this.lastPositionSeconds = positionSeconds;
		this.lastActiveAt = LocalDateTime.now();
	}
	
	/**
	 * 세션을 정상 종료한다.
	 */
	public void terminate() {
		this.status = WatchSessionStatus.TERMINATED;
		this.lastActiveAt = LocalDateTime.now();
	}
	
	/**
	 * 다른 콘텐츠 시청으로 인해 세션을 종료한다.
	 */
	public void replace() {
		this.status = WatchSessionStatus.REPLACED;
		this.lastActiveAt = LocalDateTime.now();
	}
	
	/**
	 * 타임아웃으로 세션을 종료한다.
	 */
	public void timeout() {
		this.status = WatchSessionStatus.TIMEOUT;
	}
	
	/**
	 * 세션이 활성 상태인지 확인한다.
	 *
	 * @return 활성 상태이면 true
	 */
	@JsonIgnore
	public boolean isActive() {
		return status.isActive();
	}
	
	/**
	 * 해당 사용자의 세션인지 확인한다.
	 *
	 * @param userId 확인할 사용자 ID
	 * @return 해당 사용자의 세션이면 true
	 */
	@JsonIgnore
	public boolean belongsTo(final Long userId) {
		return this.userId.equals(userId);
	}
	
	/**
	 * 해당 콘텐츠의 세션인지 확인한다.
	 *
	 * @param contentId 확인할 콘텐츠 ID
	 * @return 해당 콘텐츠의 세션이면 true
	 */
	@JsonIgnore
	public boolean isForContent(final Long contentId) {
		return this.contentId.equals(contentId);
	}
	
	private void validateActiveSession() {
		if (!isActive()) {
			throw new IllegalStateException("비활성 세션에서는 진행 상황을 업데이트할 수 없습니다.");
		}
	}
	
	// Getters
	public Long getSessionId() {
		return sessionId;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public Long getContentId() {
		return contentId;
	}
	
	public LocalDateTime getStartedAt() {
		return startedAt;
	}
	
	public LocalDateTime getLastActiveAt() {
		return lastActiveAt;
	}
	
	public WatchSessionStatus getStatus() {
		return status;
	}
	
	public Integer getLastPositionSeconds() {
		return lastPositionSeconds;
	}
}
