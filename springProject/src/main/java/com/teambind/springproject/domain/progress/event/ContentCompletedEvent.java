package com.teambind.springproject.domain.progress.event;

import java.time.LocalDateTime;

/**
 * 콘텐츠 학습 완료 이벤트.
 * Redis Pub/Sub으로 발행되는 메시지 DTO.
 */
public record ContentCompletedEvent(
		Long studentId,
		Long contentId,
		Long weekId,
		Long courseId,
		LocalDateTime completedAt
) {
	
	public static ContentCompletedEvent of(
			final Long studentId,
			final Long contentId,
			final Long weekId,
			final Long courseId,
			final LocalDateTime completedAt
	) {
		return new ContentCompletedEvent(studentId, contentId, weekId, courseId, completedAt);
	}
}
