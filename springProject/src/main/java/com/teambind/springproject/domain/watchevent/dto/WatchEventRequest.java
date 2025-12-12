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

    @NotNull(message = "이벤트 발생 시간은 필수입니다.")
    LocalDateTime timestamp,

    WatchEventPayload payload
) {

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
