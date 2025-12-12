package com.teambind.springproject.domain.session.controller;

import com.teambind.springproject.domain.session.dto.SessionResponse;
import com.teambind.springproject.domain.session.dto.SessionStartRequest;
import com.teambind.springproject.domain.session.service.WatchSessionService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시청 세션 관리 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class WatchSessionController {

  private final WatchSessionService sessionService;

  public WatchSessionController(final WatchSessionService sessionService) {
    this.sessionService = sessionService;
  }

  /**
   * 시청 세션을 시작한다.
   * 기존 활성 세션이 있으면 자동으로 종료된다.
   *
   * @param request 세션 시작 요청
   * @return 생성된 세션 정보
   */
  @PostMapping
  public ResponseEntity<SessionResponse> startSession(
      @Valid @RequestBody final SessionStartRequest request
  ) {
    SessionResponse response = sessionService.startSession(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 시청 세션을 종료한다.
   *
   * @param sessionId 세션 ID
   * @param userId 사용자 ID
   * @return 204 No Content
   */
  @DeleteMapping("/{sessionId}")
  public ResponseEntity<Void> endSession(
      @PathVariable final Long sessionId,
      @RequestParam final Long userId
  ) {
    sessionService.endSession(sessionId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 세션 정보를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 세션 정보
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<SessionResponse> getSession(
      @PathVariable final Long sessionId
  ) {
    Optional<SessionResponse> response = sessionService.getSession(sessionId);
    return response
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * 사용자의 현재 활성 세션을 조회한다.
   *
   * @param userId 사용자 ID
   * @return 활성 세션 정보
   */
  @GetMapping("/active")
  public ResponseEntity<SessionResponse> getActiveSession(
      @RequestParam final Long userId
  ) {
    Optional<SessionResponse> response = sessionService.getActiveSession(userId);
    return response
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
