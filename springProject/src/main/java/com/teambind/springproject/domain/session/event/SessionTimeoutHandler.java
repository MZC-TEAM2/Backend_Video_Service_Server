package com.teambind.springproject.domain.session.event;

import com.teambind.springproject.domain.progress.entity.StudentContentProgress;
import com.teambind.springproject.domain.progress.repository.StudentContentProgressRepository;
import com.teambind.springproject.domain.session.entity.WatchSession;
import com.teambind.springproject.domain.session.repository.WatchSessionRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 타임아웃 이벤트 핸들러.
 * 세션 만료 시 마지막 진행률을 DB에 저장한다.
 */
@Component
public class SessionTimeoutHandler {

  private static final Logger log = LoggerFactory.getLogger(SessionTimeoutHandler.class);

  private final WatchSessionRepository sessionRepository;
  private final StudentContentProgressRepository progressRepository;
  private final int completionThreshold;

  public SessionTimeoutHandler(
      final WatchSessionRepository sessionRepository,
      final StudentContentProgressRepository progressRepository,
      @Value("${learning.completion-threshold:90}") final int completionThreshold
  ) {
    this.sessionRepository = sessionRepository;
    this.progressRepository = progressRepository;
    this.completionThreshold = completionThreshold;
  }

  /**
   * 세션 타임아웃 이벤트를 처리한다.
   *
   * @param event 세션 타임아웃 이벤트
   */
  @Async
  @EventListener
  @Transactional
  public void handleSessionTimeout(final SessionTimeoutEvent event) {
    Long sessionId = event.sessionId();
    log.info("세션 타임아웃 처리 시작: sessionId={}", sessionId);

    // 백업에서 세션 정보 조회
    Optional<WatchSession> backupSession = sessionRepository.findBackupById(sessionId);

    if (backupSession.isEmpty()) {
      log.warn("타임아웃된 세션의 백업을 찾을 수 없음: sessionId={}", sessionId);
      return;
    }

    WatchSession session = backupSession.get();

    // 이미 종료된 세션이면 무시 (REPLACED, TERMINATED 등)
    if (!session.isActive()) {
      log.debug("이미 종료된 세션 무시: sessionId={}, status={}", sessionId, session.getStatus());
      sessionRepository.deleteBackup(sessionId);
      return;
    }

    // 진행률 저장
    saveProgressOnTimeout(session);

    // 백업 삭제
    sessionRepository.deleteBackup(sessionId);
    sessionRepository.deleteActiveByUserId(session.getUserId());

    log.info("세션 타임아웃 처리 완료: sessionId={}, userId={}, contentId={}",
        sessionId, session.getUserId(), session.getContentId());
  }

  private void saveProgressOnTimeout(final WatchSession session) {
    StudentContentProgress progress = progressRepository
        .findByContentIdAndStudentId(session.getContentId(), session.getUserId())
        .orElse(null);

    if (progress == null) {
      log.debug("저장할 진행 기록 없음: contentId={}, studentId={}",
          session.getContentId(), session.getUserId());
      return;
    }

    // 마지막 접근 시간 업데이트
    progress.incrementAccessCount();
    progressRepository.save(progress);

    log.debug("타임아웃 시 진행률 저장: contentId={}, studentId={}, progress={}%",
        session.getContentId(), session.getUserId(), progress.getProgressPercentage());
  }
}
