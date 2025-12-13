package com.teambind.springproject.domain.session.dto;

import com.teambind.springproject.domain.session.entity.WatchSession;
import com.teambind.springproject.domain.session.entity.WatchSessionStatus;

import java.time.LocalDateTime;

/**
 * 시청 세션 응답 DTO.
 * sessionId는 JavaScript Number 정밀도 문제로 String으로 반환한다.
 */
public record SessionResponse(
		String sessionId,
		Long userId,
		Long contentId,
		LocalDateTime startedAt,
		LocalDateTime lastActiveAt,
		WatchSessionStatus status,
		Integer lastPositionSeconds
) {
	
	/**
	 * WatchSession 엔티티로부터 응답 DTO를 생성한다.
	 *
	 * @param session 시청 세션
	 * @return SessionResponse
	 */
	public static SessionResponse from(final WatchSession session) {
		return new SessionResponse(
				String.valueOf(session.getSessionId()),
				session.getUserId(),
				session.getContentId(),
				session.getStartedAt(),
				session.getLastActiveAt(),
				session.getStatus(),
				session.getLastPositionSeconds()
		);
	}
}
