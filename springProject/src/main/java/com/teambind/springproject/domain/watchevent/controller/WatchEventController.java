package com.teambind.springproject.domain.watchevent.controller;

import com.teambind.springproject.domain.watchevent.dto.WatchEventRequest;
import com.teambind.springproject.domain.watchevent.dto.WatchEventResponse;
import com.teambind.springproject.domain.watchevent.entity.WatchEventType;
import com.teambind.springproject.domain.watchevent.service.WatchEventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시청 이벤트 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/watch-events")
public class WatchEventController {

  private final WatchEventService eventService;

  public WatchEventController(final WatchEventService eventService) {
    this.eventService = eventService;
  }

  /**
   * 시청 이벤트를 기록한다.
   *
   * @param request 이벤트 요청
   * @return 저장된 이벤트 응답
   */
  @PostMapping
  public ResponseEntity<WatchEventResponse> recordEvent(
      @Valid @RequestBody final WatchEventRequest request
  ) {
    WatchEventResponse response = eventService.recordEvent(request);
    return ResponseEntity.ok(response);
  }

  /**
   * 세션의 모든 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 이벤트 목록
   */
  @GetMapping("/session/{sessionId}")
  public ResponseEntity<List<WatchEventResponse>> getEventsBySession(
      @PathVariable final Long sessionId
  ) {
    List<WatchEventResponse> events = eventService.getEventsBySession(sessionId);
    return ResponseEntity.ok(events);
  }

  /**
   * 세션의 특정 타입 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @param type 이벤트 타입
   * @return 이벤트 목록
   */
  @GetMapping("/session/{sessionId}/type")
  public ResponseEntity<List<WatchEventResponse>> getEventsBySessionAndType(
      @PathVariable final Long sessionId,
      @RequestParam final WatchEventType type
  ) {
    List<WatchEventResponse> events = eventService.getEventsBySessionAndType(sessionId, type);
    return ResponseEntity.ok(events);
  }
}
