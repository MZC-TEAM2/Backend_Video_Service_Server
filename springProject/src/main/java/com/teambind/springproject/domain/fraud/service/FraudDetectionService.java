package com.teambind.springproject.domain.fraud.service;

import com.teambind.springproject.domain.fraud.entity.FraudRecord;
import com.teambind.springproject.domain.fraud.entity.WatchedSegment;
import com.teambind.springproject.domain.fraud.repository.FraudRecordRepository;
import com.teambind.springproject.domain.fraud.repository.WatchedSegmentRepository;
import com.teambind.springproject.domain.watchevent.entity.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부정 시청 감지 서비스.
 */
@Service
public class FraudDetectionService {
	
	private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);
	
	private final FraudRecordRepository fraudRepository;
	private final WatchedSegmentRepository segmentRepository;
	private final int skipAllowanceSeconds;
	
	public FraudDetectionService(
			final FraudRecordRepository fraudRepository,
			final WatchedSegmentRepository segmentRepository,
			@Value("${fraud.skip-allowance-seconds:5}") final int skipAllowanceSeconds
	) {
		this.fraudRepository = fraudRepository;
		this.segmentRepository = segmentRepository;
		this.skipAllowanceSeconds = skipAllowanceSeconds;
	}
	
	/**
	 * 이벤트를 분석하여 부정 시청을 감지한다.
	 *
	 * @param event 시청 이벤트
	 */
	@Transactional
	public void analyzeEvent(final WatchEvent event) {
		switch (event.getEventType()) {
			case SEEK -> handleSeekEvent(event);
			case RATE_CHANGE -> handleRateChangeEvent(event);
			case VISIBILITY_HIDDEN -> handleVisibilityHiddenEvent(event);
			case PLAY -> handlePlayEvent(event);
			default -> {
				// PAUSE, VISIBILITY_VISIBLE 등은 별도 처리 없음
			}
		}
	}
	
	/**
	 * 시청 구간을 기록한다.
	 *
	 * @param sessionId    세션 ID
	 * @param userId       사용자 ID
	 * @param contentId    콘텐츠 ID
	 * @param startSeconds 시작 시간
	 * @param endSeconds   종료 시간
	 * @param playbackRate 재생 속도
	 */
	@Transactional
	public void recordWatchedSegment(
			final Long sessionId,
			final Long userId,
			final Long contentId,
			final Integer startSeconds,
			final Integer endSeconds,
			final Double playbackRate
	) {
		if (startSeconds == null || endSeconds == null || startSeconds >= endSeconds) {
			return;
		}
		
		WatchedSegment segment = WatchedSegment.createValid(
				sessionId, userId, contentId, startSeconds, endSeconds, playbackRate
		);
		segmentRepository.save(segment);
		
		log.debug("시청 구간 기록: sessionId={}, {}초~{}초, rate={}, valid={}",
				sessionId, startSeconds, endSeconds, playbackRate, segment.getIsValid());
	}
	
	/**
	 * 사용자의 콘텐츠별 유효 시청 시간을 조회한다.
	 *
	 * @param userId    사용자 ID
	 * @param contentId 콘텐츠 ID
	 * @return 유효 시청 시간(초)
	 */
	@Transactional(readOnly = true)
	public int getValidWatchedSeconds(final Long userId, final Long contentId) {
		return segmentRepository.sumValidWatchedSeconds(userId, contentId);
	}
	
	/**
	 * 세션의 부정 기록 수를 조회한다.
	 *
	 * @param sessionId 세션 ID
	 * @return 부정 기록 수
	 */
	@Transactional(readOnly = true)
	public long getFraudCount(final Long sessionId) {
		return fraudRepository.countBySessionId(sessionId);
	}
	
	private void handleSeekEvent(final WatchEvent event) {
		Integer from = event.getFromPositionSeconds();
		Integer to = event.getToPositionSeconds();
		
		if (from == null || to == null) {
			return;
		}
		
		int skipped = to - from;
		
		// 5초 이내 스킵은 허용
		if (skipped <= skipAllowanceSeconds) {
			log.debug("허용된 스킵: {}초 -> {}초 ({}초)", from, to, skipped);
			return;
		}
		
		// 뒤로 이동(되감기)은 부정 아님
		if (skipped < 0) {
			return;
		}
		
		// 스킵 구간 부정 기록
		FraudRecord fraud = FraudRecord.skipSegment(
				event.getSessionId(),
				event.getUserId(),
				event.getContentId(),
				from,
				to
		);
		fraudRepository.save(fraud);
		
		// 스킵 구간은 무효 시청으로 기록
		WatchedSegment invalidSegment = WatchedSegment.createInvalid(
				event.getSessionId(),
				event.getUserId(),
				event.getContentId(),
				from,
				to
		);
		segmentRepository.save(invalidSegment);
		
		log.info("스킵 감지: sessionId={}, {}초 -> {}초 ({}초 스킵)",
				event.getSessionId(), from, to, skipped);
	}
	
	private void handleRateChangeEvent(final WatchEvent event) {
		Double rate = event.getPlaybackRate();
		
		if (rate == null || rate <= 1.0) {
			return;
		}
		
		// 배속 재생 부정 기록
		FraudRecord fraud = FraudRecord.fastPlayback(
				event.getSessionId(),
				event.getUserId(),
				event.getContentId(),
				rate
		);
		fraudRepository.save(fraud);
		
		log.info("배속 재생 감지: sessionId={}, rate={}x", event.getSessionId(), rate);
	}
	
	private void handleVisibilityHiddenEvent(final WatchEvent event) {
		// VISIBILITY_HIDDEN 이벤트 발생 시점 기록
		// 실제 무효 처리는 VISIBILITY_VISIBLE 이벤트 시 구간으로 계산
		log.debug("탭 숨김 감지: sessionId={}, position={}",
				event.getSessionId(), event.getPositionSeconds());
	}
	
	private void handlePlayEvent(final WatchEvent event) {
		// PLAY 이벤트는 시청 시작 지점 기록용
		log.debug("재생 시작: sessionId={}, position={}",
				event.getSessionId(), event.getPositionSeconds());
	}
}
