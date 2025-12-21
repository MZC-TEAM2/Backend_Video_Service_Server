package com.teambind.springproject.domain.session.service;

import com.teambind.springproject.common.util.generator.PrimaryKeyGenerator;
import com.teambind.springproject.domain.session.dto.SessionResponse;
import com.teambind.springproject.domain.session.dto.SessionStartRequest;
import com.teambind.springproject.domain.session.entity.WatchSession;
import com.teambind.springproject.domain.session.repository.WatchSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 시청 세션 관리 서비스.
 */
@Service
public class WatchSessionService {
	
	private static final Logger log = LoggerFactory.getLogger(WatchSessionService.class);
	
	private final WatchSessionRepository sessionRepository;
	private final PrimaryKeyGenerator keyGenerator;
	
	public WatchSessionService(
			final WatchSessionRepository sessionRepository,
			final PrimaryKeyGenerator keyGenerator
	) {
		this.sessionRepository = sessionRepository;
		this.keyGenerator = keyGenerator;
	}
	
	/**
	 * 새로운 시청 세션을 시작한다.
	 * 기존 활성 세션이 있으면 종료하고 새 세션을 생성한다.
	 *
	 * @param request 세션 시작 요청
	 * @return 생성된 세션 응답
	 */
	public SessionResponse startSession(final SessionStartRequest request) {
		// 기존 활성 세션 종료
		terminateExistingSession(request.userId());
		
		// 새 세션 생성
		Long sessionId = keyGenerator.generateLongKey();
		WatchSession session = WatchSession.create(
				sessionId,
				request.userId(),
				request.contentId()
		);
		
		log.info("세션 생성: sessionId={}, userId={}, contentId={}",
				sessionId, request.userId(), request.contentId());
		
		sessionRepository.save(session);
		
		// 저장 확인
		Optional<WatchSession> savedSession = sessionRepository.findById(sessionId);
		log.info("세션 저장 확인: sessionId={}, found={}", sessionId, savedSession.isPresent());
		
		return SessionResponse.from(session);
	}
	
	/**
	 * 세션을 종료한다.
	 *
	 * @param sessionId 세션 ID
	 * @param userId    사용자 ID (세션 소유자 검증용)
	 */
	public void endSession(final Long sessionId, final Long userId) {
		Optional<WatchSession> sessionOpt = sessionRepository.findById(sessionId);
		
		if (sessionOpt.isEmpty()) {
			return;
		}
		
		WatchSession session = sessionOpt.get();
		
		if (!session.belongsTo(userId)) {
			throw new IllegalArgumentException("해당 세션에 대한 권한이 없습니다.");
		}
		
		session.terminate();
		sessionRepository.save(session);
		sessionRepository.deleteActiveByUserId(userId);
	}
	
	/**
	 * 사용자의 현재 활성 세션을 조회한다.
	 *
	 * @param userId 사용자 ID
	 * @return 활성 세션 응답 (Optional)
	 */
	public Optional<SessionResponse> getActiveSession(final Long userId) {
		return sessionRepository.findActiveByUserId(userId)
				.map(SessionResponse::from);
	}
	
	/**
	 * 세션 ID로 세션을 조회한다.
	 *
	 * @param sessionId 세션 ID
	 * @return 세션 응답 (Optional)
	 */
	public Optional<SessionResponse> getSession(final Long sessionId) {
		return sessionRepository.findById(sessionId)
				.map(SessionResponse::from);
	}
	
	/**
	 * 기존 활성 세션을 종료한다.
	 *
	 * @param userId 사용자 ID
	 */
	private void terminateExistingSession(final Long userId) {
		Optional<WatchSession> existingSession = sessionRepository.findActiveByUserId(userId);
		
		if (existingSession.isPresent()) {
			WatchSession session = existingSession.get();
			session.replace();
			sessionRepository.save(session);
			sessionRepository.deleteActiveByUserId(userId);
		}
	}
}
