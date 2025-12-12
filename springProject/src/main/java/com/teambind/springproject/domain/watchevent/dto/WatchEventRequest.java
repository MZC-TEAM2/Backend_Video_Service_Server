package com.teambind.springproject.domain.watchevent.dto;

import com.teambind.springproject.domain.watchevent.entity.WatchEventType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 시청 이벤트 요청 DTO.
 */
public record WatchEventRequest(
    @NotNull(message = "세션 ID는 필수입니다.")
    Long sessionId,

    @NotNull(message = "이벤트 타입은 필수입니다.")
    WatchEventType eventType,

    LocalDateTime timestamp,

    WatchEventPayload payload
) {

  /**
   * 타임스탬프를 반환한다. null이면 현재 시간을 반환한다.
   */
  public LocalDateTime getTimestampOrNow() {
    return timestamp != null ? timestamp : LocalDateTime.now();
  }

  /**
   * 이벤트 페이로드.
   * 이벤트 타입에 따라 필요한 필드가 다르다.
   */
  public record WatchEventPayload(
      Integer positionSeconds,
      Integer fromPosition,
      Integer toPosition,
      Double rate
  ) {
  }
}
