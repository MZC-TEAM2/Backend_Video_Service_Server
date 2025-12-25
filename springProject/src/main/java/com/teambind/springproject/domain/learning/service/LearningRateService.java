package com.teambind.springproject.domain.learning.service;

import com.teambind.springproject.domain.fraud.repository.WatchedSegmentRepository;
import com.teambind.springproject.domain.progress.entity.StudentContentProgress;
import com.teambind.springproject.domain.progress.event.ContentCompletedPublisher;
import com.teambind.springproject.domain.progress.repository.StudentContentProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 학습률 계산 서비스.
 * 부정 시청을 제외한 실제 시청 시간 기반으로 학습률을 계산한다.
 */
@Service
public class LearningRateService {
	
	private static final Logger log = LoggerFactory.getLogger(LearningRateService.class);
	
	private final WatchedSegmentRepository segmentRepository;
	private final StudentContentProgressRepository progressRepository;
	private final ContentCompletedPublisher contentCompletedPublisher;
	private final int completionThreshold;
	
	public LearningRateService(
			final WatchedSegmentRepository segmentRepository,
			final StudentContentProgressRepository progressRepository,
			final ContentCompletedPublisher contentCompletedPublisher,
			@Value("${learning.completion-threshold:90}") final int completionThreshold
	) {
		this.segmentRepository = segmentRepository;
		this.progressRepository = progressRepository;
		this.contentCompletedPublisher = contentCompletedPublisher;
		this.completionThreshold = completionThreshold;
	}
	
	/**
	 * 학습률을 계산하고 업데이트한다.
	 *
	 * @param userId               사용자 ID
	 * @param contentId            콘텐츠 ID
	 * @param totalDurationSeconds 전체 영상 길이(초)
	 * @return 계산된 학습률 (0-100)
	 */
	@Transactional
	public int calculateAndUpdateLearningRate(
			final Long userId,
			final Long contentId,
			final Integer totalDurationSeconds
	) {
		if (totalDurationSeconds == null || totalDurationSeconds <= 0) {
			log.warn("유효하지 않은 영상 길이: userId={}, contentId={}, duration={}",
					userId, contentId, totalDurationSeconds);
			return 0;
		}
		
		// 유효 시청 시간 조회
		int validWatchedSeconds = segmentRepository.sumValidWatchedSeconds(userId, contentId);
		
		// 학습률 계산 (최대 100%)
		int learningRate = Math.min(
				(int) ((validWatchedSeconds * 100.0) / totalDurationSeconds),
				100
		);
		
		// 진행 상황 업데이트
		updateProgress(userId, contentId, learningRate, validWatchedSeconds);
		
		log.debug("학습률 계산: userId={}, contentId={}, validSeconds={}, total={}, rate={}%",
				userId, contentId, validWatchedSeconds, totalDurationSeconds, learningRate);
		
		return learningRate;
	}
	
	/**
	 * 현재 학습률을 조회한다.
	 *
	 * @param userId               사용자 ID
	 * @param contentId            콘텐츠 ID
	 * @param totalDurationSeconds 전체 영상 길이(초)
	 * @return 학습률 (0-100)
	 */
	@Transactional(readOnly = true)
	public int getLearningRate(
			final Long userId,
			final Long contentId,
			final Integer totalDurationSeconds
	) {
		if (totalDurationSeconds == null || totalDurationSeconds <= 0) {
			return 0;
		}
		
		int validWatchedSeconds = segmentRepository.sumValidWatchedSeconds(userId, contentId);
		return Math.min(
				(int) ((validWatchedSeconds * 100.0) / totalDurationSeconds),
				100
		);
	}
	
	/**
	 * 유효 시청 시간을 조회한다.
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
	 * 완료 여부를 확인한다.
	 *
	 * @param userId               사용자 ID
	 * @param contentId            콘텐츠 ID
	 * @param totalDurationSeconds 전체 영상 길이(초)
	 * @return 완료 여부
	 */
	@Transactional(readOnly = true)
	public boolean isCompleted(
			final Long userId,
			final Long contentId,
			final Integer totalDurationSeconds
	) {
		int learningRate = getLearningRate(userId, contentId, totalDurationSeconds);
		return learningRate >= completionThreshold;
	}
	
	private void updateProgress(
			final Long userId,
			final Long contentId,
			final int learningRate,
			final int validWatchedSeconds
	) {
		Optional<StudentContentProgress> progressOpt =
				progressRepository.findByContentIdAndStudentId(contentId, userId);
		
		if (progressOpt.isEmpty()) {
			log.debug("진행 기록 없음: userId={}, contentId={}", userId, contentId);
			return;
		}
		
		StudentContentProgress progress = progressOpt.get();
		
		// 학습률 기반으로 진행률 업데이트
		boolean justCompleted = progress.updateLearningRate(learningRate, completionThreshold);
		
		progressRepository.save(progress);
		
		// 완료 이벤트 발행
		if (justCompleted) {
			contentCompletedPublisher.publish(userId, contentId, progress.getCompletedAt());
		}
		
		log.debug("진행 상황 업데이트: userId={}, contentId={}, rate={}%, completed={}",
				userId, contentId, learningRate, progress.getIsCompleted());
	}
}
