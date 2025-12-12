package com.teambind.springproject.domain.watchevent.service;

import com.teambind.springproject.domain.fraud.service.FraudDetectionService;
import com.teambind.springproject.domain.session.entity.WatchSession;
import com.teambind.springproject.domain.session.repository.WatchSessionRepository;
import com.teambind.springproject.domain.watchevent.dto.WatchEventRequest;
import com.teambind.springproject.domain.watchevent.dto.WatchEventRequest.WatchEventPayload;
import com.teambind.springproject.domain.watchevent.dto.WatchEventResponse;
import com.teambind.springproject.domain.watchevent.entity.WatchEvent;
import com.teambind.springproject.domain.watchevent.entity.WatchEventType;
import com.teambind.springproject.domain.watchevent.repository.WatchEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시청 이벤트 서비스.
 */
@Service
public class WatchEventService {

  private static final Logger log = LoggerFactory.getLogger(WatchEventService.class);

  private final WatchEventRepository eventRepository;
  private final WatchSessionRepository sessionRepository;
  private final FraudDetectionService fraudDetectionService;

  public WatchEventService(
      final WatchEventRepository eventRepository,
      final WatchSessionRepository sessionRepository,
      final FraudDetectionService fraudDetectionService
  ) {
    this.eventRepository = eventRepository;
    this.sessionRepository = sessionRepository;
    this.fraudDetectionService = fraudDetectionService;
  }

  /**
   * 시청 이벤트를 기록한다.
   *
   * @param request 이벤트 요청
   * @return 저장된 이벤트 응답
   */
  @Transactional
  public WatchEventResponse recordEvent(final WatchEventRequest request) {
    // 세션 검증
    WatchSession session = validateAndGetSession(request.sessionId());

    // 이벤트 생성
    WatchEvent event = createEventFromRequest(request, session);

    // 저장
    WatchEvent savedEvent = eventRepository.save(event);

    // 부정 시청 감지
    fraudDetectionService.analyzeEvent(savedEvent);

    log.debug("시청 이벤트 기록: sessionId={}, type={}, userId={}, contentId={}",
        request.sessionId(), request.eventType(), session.getUserId(), session.getContentId());

    return WatchEventResponse.from(savedEvent);
  }

  /**
   * 세션의 모든 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 이벤트 목록
   */
  @Transactional(readOnly = true)
  public List<WatchEventResponse> getEventsBySession(final Long sessionId) {
    return eventRepository.findBySessionId(sessionId)
        .stream()
        .map(WatchEventResponse::from)
        .toList();
  }

  /**
   * 세션의 특정 타입 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @param eventType 이벤트 타입
   * @return 이벤트 목록
   */
  @Transactional(readOnly = true)
  public List<WatchEventResponse> getEventsBySessionAndType(
      final Long sessionId,
      final WatchEventType eventType
  ) {
    return eventRepository.findBySessionIdAndEventType(sessionId, eventType)
        .stream()
        .map(WatchEventResponse::from)
        .toList();
  }

  private WatchSession validateAndGetSession(final Long sessionId) {
    Optional<WatchSession> sessionOpt = sessionRepository.findById(sessionId);

    if (sessionOpt.isEmpty()) {
      // 백업에서 조회 시도
      sessionOpt = sessionRepository.findBackupById(sessionId);
    }

    if (sessionOpt.isEmpty()) {
      throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
    }

    return sessionOpt.get();
  }

  private WatchEvent createEventFromRequest(
      final WatchEventRequest request,
      final WatchSession session
  ) {
    WatchEventType eventType = request.eventType();
    WatchEventPayload payload = request.payload();
    LocalDateTime timestamp = request.getTimestampOrNow();

    return switch (eventType) {
      case PLAY, PAUSE -> WatchEvent.createPlaybackEvent(
          request.sessionId(),
          session.getUserId(),
          session.getContentId(),
          eventType,
          timestamp,
          payload != null ? payload.positionSeconds() : null
      );
      case SEEK -> WatchEvent.createSeekEvent(
          request.sessionId(),
          session.getUserId(),
          session.getContentId(),
          timestamp,
          payload != null ? payload.fromPosition() : null,
          payload != null ? payload.toPosition() : null
      );
      case RATE_CHANGE -> WatchEvent.createRateChangeEvent(
          request.sessionId(),
          session.getUserId(),
          session.getContentId(),
          timestamp,
          payload != null ? payload.rate() : null
      );
      case VISIBILITY_HIDDEN, VISIBILITY_VISIBLE -> WatchEvent.createVisibilityEvent(
          request.sessionId(),
          session.getUserId(),
          session.getContentId(),
          eventType,
          timestamp
      );
    };
  }
}
