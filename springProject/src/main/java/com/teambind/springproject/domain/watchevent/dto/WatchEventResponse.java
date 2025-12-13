package com.teambind.springproject.domain.watchevent.dto;

import com.teambind.springproject.domain.watchevent.entity.WatchEvent;
import com.teambind.springproject.domain.watchevent.entity.WatchEventType;

import java.time.LocalDateTime;

/**
 * 시청 이벤트 응답 DTO.
 */
public record WatchEventResponse(
		Long id,
		Long sessionId,
		Long userId,
		Long contentId,
		WatchEventType eventType,
		LocalDateTime eventTimestamp,
		Integer positionSeconds,
		Integer fromPositionSeconds,
		Integer toPositionSeconds,
		Double playbackRate,
		LocalDateTime createdAt
) {
	
	/**
	 * WatchEvent 엔티티로부터 응답 DTO를 생성한다.
	 *
	 * @param event 시청 이벤트
	 * @return WatchEventResponse
	 */
	public static WatchEventResponse from(final WatchEvent event) {
		return new WatchEventResponse(
				event.getId(),
				event.getSessionId(),
				event.getUserId(),
				event.getContentId(),
				event.getEventType(),
				event.getEventTimestamp(),
				event.getPositionSeconds(),
				event.getFromPositionSeconds(),
				event.getToPositionSeconds(),
				event.getPlaybackRate(),
				event.getCreatedAt()
		);
	}
}
