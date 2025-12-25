package com.teambind.springproject.domain.progress.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 학생 콘텐츠 진행 상황 엔티티.
 * LMS DB의 student_content_progress 테이블과 매핑.
 */
@Entity
@Table(
		name = "student_content_progress",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = {"content_id", "student_id"})
		},
		indexes = {
				@Index(name = "idx_progress_content", columnList = "content_id"),
				@Index(name = "idx_progress_student", columnList = "student_id"),
				@Index(name = "idx_progress_completed", columnList = "is_completed")
		}
)
public class StudentContentProgress {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "content_id", nullable = false)
	private Long contentId;
	
	@Column(name = "student_id", nullable = false)
	private Long studentId;
	
	@Column(name = "is_completed", nullable = false)
	private Boolean isCompleted = false;
	
	@Column(name = "progress_percentage", nullable = false)
	private Integer progressPercentage = 0;
	
	@Column(name = "last_position_seconds")
	private Integer lastPositionSeconds;
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	@Column(name = "first_accessed_at")
	private LocalDateTime firstAccessedAt;
	
	@Column(name = "last_accessed_at")
	private LocalDateTime lastAccessedAt;
	
	@Column(name = "access_count", nullable = false)
	private Integer accessCount = 0;
	
	protected StudentContentProgress() {
	}
	
	private StudentContentProgress(
			final Long contentId,
			final Long studentId
	) {
		this.contentId = contentId;
		this.studentId = studentId;
		this.isCompleted = false;
		this.progressPercentage = 0;
		this.lastPositionSeconds = 0;
		this.accessCount = 0;
		this.firstAccessedAt = LocalDateTime.now();
		this.lastAccessedAt = LocalDateTime.now();
	}
	
	/**
	 * 새로운 진행 상황을 생성한다.
	 *
	 * @param contentId 콘텐츠 ID
	 * @param studentId 학생 ID
	 * @return StudentContentProgress 인스턴스
	 */
	public static StudentContentProgress create(
			final Long contentId,
			final Long studentId
	) {
		return new StudentContentProgress(contentId, studentId);
	}
	
	/**
	 * 시청 위치를 업데이트한다.
	 *
	 * @param positionSeconds      현재 시청 위치 (초)
	 * @param totalDurationSeconds 전체 영상 길이 (초)
	 * @param completionThreshold  완료 기준 퍼센트 (예: 90)
	 * @return 이번 호출로 완료 처리가 발생했으면 true
	 */
	public boolean updateProgress(
			final Integer positionSeconds,
			final Integer totalDurationSeconds,
			final int completionThreshold
	) {
		this.lastPositionSeconds = positionSeconds;
		this.lastAccessedAt = LocalDateTime.now();
		this.accessCount++;
		
		// 진행률 계산
		if (totalDurationSeconds > 0) {
			int newPercentage = (int) ((positionSeconds * 100.0) / totalDurationSeconds);
			this.progressPercentage = Math.min(Math.max(newPercentage, this.progressPercentage), 100);
		}
		
		// 완료 처리
		if (!this.isCompleted && this.progressPercentage >= completionThreshold) {
			this.isCompleted = true;
			this.completedAt = LocalDateTime.now();
			return true;
		}
		return false;
	}
	
	/**
	 * 접근 횟수를 증가시킨다.
	 */
	public void incrementAccessCount() {
		this.accessCount++;
		this.lastAccessedAt = LocalDateTime.now();
	}
	
	/**
	 * 학습률 기반으로 진행률을 업데이트한다.
	 * 부정 시청을 제외한 유효 시청 시간 기반 학습률을 적용한다.
	 *
	 * @param learningRate        학습률 (0-100)
	 * @param completionThreshold 완료 기준 퍼센트
	 * @return 이번 호출로 완료 처리가 발생했으면 true
	 */
	public boolean updateLearningRate(final int learningRate, final int completionThreshold) {
		// 학습률이 기존 진행률보다 높을 때만 업데이트
		if (learningRate > this.progressPercentage) {
			this.progressPercentage = learningRate;
		}
		this.lastAccessedAt = LocalDateTime.now();
		
		// 완료 처리
		if (!this.isCompleted && this.progressPercentage >= completionThreshold) {
			this.isCompleted = true;
			this.completedAt = LocalDateTime.now();
			return true;
		}
		return false;
	}
	
	// Getters
	public Long getId() {
		return id;
	}
	
	public Long getContentId() {
		return contentId;
	}
	
	public Long getStudentId() {
		return studentId;
	}
	
	public Boolean getIsCompleted() {
		return isCompleted;
	}
	
	public Integer getProgressPercentage() {
		return progressPercentage;
	}
	
	public Integer getLastPositionSeconds() {
		return lastPositionSeconds;
	}
	
	public LocalDateTime getCompletedAt() {
		return completedAt;
	}
	
	public LocalDateTime getFirstAccessedAt() {
		return firstAccessedAt;
	}
	
	public LocalDateTime getLastAccessedAt() {
		return lastAccessedAt;
	}
	
	public Integer getAccessCount() {
		return accessCount;
	}
}
