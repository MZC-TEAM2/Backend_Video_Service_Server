package com.teambind.springproject.domain.progress.service;

import com.teambind.springproject.domain.learning.service.LearningRateService;
import com.teambind.springproject.domain.progress.dto.ProgressReportRequest;
import com.teambind.springproject.domain.progress.dto.ProgressResponse;
import com.teambind.springproject.domain.progress.entity.StudentContentProgress;
import com.teambind.springproject.domain.progress.repository.StudentContentProgressRepository;
import com.teambind.springproject.domain.session.entity.WatchSession;
import com.teambind.springproject.domain.session.repository.WatchSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 시청 진행 상황 서비스.
 */
@Service
public class ProgressService {
	
	private final StudentContentProgressRepository progressRepository;
	private final WatchSessionRepository sessionRepository;
	private final LearningRateService learningRateService;
	private final int completionThreshold;
	private final long sessionTimeoutSeconds;
	
	public ProgressService(
			final StudentContentProgressRepository progressRepository,
			final WatchSessionRepository sessionRepository,
			final LearningRateService learningRateService,
			@Value("${learning.completion-threshold:90}") final int completionThreshold,
			@Value("${watch.session.timeout-seconds:30}") final long sessionTimeoutSeconds
	) {
		this.progressRepository = progressRepository;
		this.sessionRepository = sessionRepository;
		this.learningRateService = learningRateService;
		this.completionThreshold = completionThreshold;
		this.sessionTimeoutSeconds = sessionTimeoutSeconds;
	}
	
	/**
	 * 시청 진행 상황을 보고한다.
	 *
	 * @param request 진행 상황 보고 요청
	 * @return 업데이트된 진행 상황 응답
	 */
	@Transactional
	public ProgressResponse reportProgress(final ProgressReportRequest request) {
		// 세션 검증
		WatchSession session = validateAndGetSession(request.getSessionIdAsLong());
		
		// 콘텐츠 ID 일치 확인
		if (!session.isForContent(request.contentId())) {
			throw new IllegalArgumentException("세션의 콘텐츠 ID와 요청의 콘텐츠 ID가 일치하지 않습니다.");
		}
		
		// 세션 진행 상황 업데이트
		session.updateProgress(request.currentPositionSeconds());
		sessionRepository.save(session);
		sessionRepository.refreshTtl(session.getSessionId(), sessionTimeoutSeconds);
		
		// DB 진행 상황 업데이트
		StudentContentProgress progress = getOrCreateProgress(
				request.contentId(),
				session.getUserId()
		);
		
		progress.updateProgress(
				request.currentPositionSeconds(),
				request.totalDurationSeconds(),
				completionThreshold
		);
		
		progressRepository.save(progress);
		
		// 학습률 기반 진행률 업데이트 (부정 시청 제외)
		learningRateService.calculateAndUpdateLearningRate(
				session.getUserId(),
				request.contentId(),
				request.totalDurationSeconds()
		);
		
		// 업데이트된 진행 상황 다시 조회
		StudentContentProgress updatedProgress = progressRepository
				.findByContentIdAndStudentId(request.contentId(), session.getUserId())
				.orElse(progress);
		
		return ProgressResponse.from(updatedProgress);
	}
	
	/**
	 * 콘텐츠별 진행 상황을 조회한다.
	 *
	 * @param contentId 콘텐츠 ID
	 * @param studentId 학생 ID
	 * @return 진행 상황 응답 (Optional)
	 */
	@Transactional(readOnly = true)
	public Optional<ProgressResponse> getProgress(final Long contentId, final Long studentId) {
		return progressRepository.findByContentIdAndStudentId(contentId, studentId)
				.map(ProgressResponse::from);
	}
	
	private WatchSession validateAndGetSession(final Long sessionId) {
		Optional<WatchSession> sessionOpt = sessionRepository.findById(sessionId);
		
		if (sessionOpt.isEmpty()) {
			// 백업에서 조회 시도
			sessionOpt = sessionRepository.findBackupById(sessionId);
		}
		
		if (sessionOpt.isEmpty()) {
			throw new IllegalArgumentException("세션을 찾을 수 없습니다.");
		}
		
		WatchSession session = sessionOpt.get();
		
		if (!session.isActive()) {
			throw new IllegalStateException("비활성 세션입니다. 새로운 세션을 시작해주세요.");
		}
		
		return session;
	}
	
	private StudentContentProgress getOrCreateProgress(
			final Long contentId,
			final Long studentId
	) {
		return progressRepository.findByContentIdAndStudentId(contentId, studentId)
				.orElseGet(() -> StudentContentProgress.create(contentId, studentId));
	}
}
