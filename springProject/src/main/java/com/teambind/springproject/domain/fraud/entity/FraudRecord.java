package com.teambind.springproject.domain.fraud.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 부정 시청 기록 엔티티.
 */
@Entity
@Table(
		name = "fraud_record",
		indexes = {
				@Index(name = "idx_fraud_session", columnList = "session_id"),
				@Index(name = "idx_fraud_user_content", columnList = "user_id, content_id"),
				@Index(name = "idx_fraud_type", columnList = "fraud_type")
		}
)
public class FraudRecord {
	
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
	@Column(name = "fraud_type", nullable = false, length = 30)
	private FraudType fraudType;
	
	@Column(name = "start_seconds")
	private Integer startSeconds;
	
	@Column(name = "end_seconds")
	private Integer endSeconds;
	
	@Column(name = "detail", length = 500)
	private String detail;
	
	@Column(name = "detected_at", nullable = false)
	private LocalDateTime detectedAt;
	
	protected FraudRecord() {
	}
	
	private FraudRecord(
			final Long sessionId,
			final Long userId,
			final Long contentId,
			final FraudType fraudType,
			final Integer startSeconds,
			final Integer endSeconds,
			final String detail
	) {
		this.sessionId = sessionId;
		this.userId = userId;
		this.contentId = contentId;
		this.fraudType = fraudType;
		this.startSeconds = startSeconds;
		this.endSeconds = endSeconds;
		this.detail = detail;
		this.detectedAt = LocalDateTime.now();
	}
	
	/**
	 * 배속 재생 부정 기록을 생성한다.
	 *
	 * @param sessionId    세션 ID
	 * @param userId       사용자 ID
	 * @param contentId    콘텐츠 ID
	 * @param playbackRate 재생 속도
	 * @return FraudRecord 인스턴스
	 */
	public static FraudRecord fastPlayback(
			final Long sessionId,
			final Long userId,
			final Long contentId,
			final Double playbackRate
	) {
		return new FraudRecord(
				sessionId, userId, contentId, FraudType.FAST_PLAYBACK,
				null, null, "재생 속도: " + playbackRate + "x"
		);
	}
	
	/**
	 * 탭 숨김 부정 기록을 생성한다.
	 *
	 * @param sessionId    세션 ID
	 * @param userId       사용자 ID
	 * @param contentId    콘텐츠 ID
	 * @param startSeconds 숨김 시작 위치
	 * @param endSeconds   숨김 종료 위치
	 * @return FraudRecord 인스턴스
	 */
	public static FraudRecord tabHidden(
			final Long sessionId,
			final Long userId,
			final Long contentId,
			final Integer startSeconds,
			final Integer endSeconds
	) {
		int duration = endSeconds - startSeconds;
		return new FraudRecord(
				sessionId, userId, contentId, FraudType.TAB_HIDDEN,
				startSeconds, endSeconds, "탭 숨김 " + duration + "초"
		);
	}
	
	/**
	 * 구간 스킵 부정 기록을 생성한다.
	 *
	 * @param sessionId   세션 ID
	 * @param userId      사용자 ID
	 * @param contentId   콘텐츠 ID
	 * @param fromSeconds 스킵 시작 위치
	 * @param toSeconds   스킵 종료 위치
	 * @return FraudRecord 인스턴스
	 */
	public static FraudRecord skipSegment(
			final Long sessionId,
			final Long userId,
			final Long contentId,
			final Integer fromSeconds,
			final Integer toSeconds
	) {
		int skipped = toSeconds - fromSeconds;
		return new FraudRecord(
				sessionId, userId, contentId, FraudType.SKIP_SEGMENT,
				fromSeconds, toSeconds, "스킵 " + skipped + "초"
		);
	}
	
	/**
	 * 부정 구간의 길이를 반환한다.
	 *
	 * @return 구간 길이(초), 구간이 없으면 0
	 */
	public int getDuration() {
		if (startSeconds == null || endSeconds == null) {
			return 0;
		}
		return endSeconds - startSeconds;
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
	
	public FraudType getFraudType() {
		return fraudType;
	}
	
	public Integer getStartSeconds() {
		return startSeconds;
	}
	
	public Integer getEndSeconds() {
		return endSeconds;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public LocalDateTime getDetectedAt() {
		return detectedAt;
	}
}
