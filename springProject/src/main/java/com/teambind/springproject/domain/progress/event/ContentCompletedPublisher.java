package com.teambind.springproject.domain.progress.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.springproject.domain.content.repository.WeekContentRepository;
import com.teambind.springproject.domain.week.repository.CourseWeekRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 콘텐츠 학습 완료 이벤트 발행 서비스.
 * Redis Pub/Sub으로 이벤트를 발행한다.
 */
@Service
public class ContentCompletedPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(ContentCompletedPublisher.class);
	private static final String CHANNEL = "attendance:content-completed";
	
	private final RedisTemplate<String, Object> redisTemplate;
	private final WeekContentRepository weekContentRepository;
	private final CourseWeekRepository courseWeekRepository;
	private final ObjectMapper objectMapper;
	
	public ContentCompletedPublisher(
			final RedisTemplate<String, Object> redisTemplate,
			final WeekContentRepository weekContentRepository,
			final CourseWeekRepository courseWeekRepository,
			final ObjectMapper objectMapper
	) {
		this.redisTemplate = redisTemplate;
		this.weekContentRepository = weekContentRepository;
		this.courseWeekRepository = courseWeekRepository;
		this.objectMapper = objectMapper;
	}
	
	/**
	 * 콘텐츠 학습 완료 이벤트를 발행한다.
	 *
	 * @param studentId   학생 ID
	 * @param contentId   콘텐츠 ID
	 * @param completedAt 완료 시각
	 */
	public void publish(
			final Long studentId,
			final Long contentId,
			final LocalDateTime completedAt
	) {
		try {
			// weekId 조회
			Long weekId = weekContentRepository.findById(contentId)
					.map(content -> content.getWeekId())
					.orElse(null);
			
			if (weekId == null) {
				log.warn("콘텐츠를 찾을 수 없습니다: contentId={}", contentId);
				return;
			}
			
			// courseId 조회
			Long courseId = courseWeekRepository.findById(weekId)
					.map(week -> week.getCourseId())
					.orElse(null);
			
			if (courseId == null) {
				log.warn("주차를 찾을 수 없습니다: weekId={}", weekId);
				return;
			}
			
			// 이벤트 생성 및 발행
			ContentCompletedEvent event = ContentCompletedEvent.of(
					studentId,
					contentId,
					weekId,
					courseId,
					completedAt
			);
			
			String message = objectMapper.writeValueAsString(event);
			redisTemplate.convertAndSend(CHANNEL, message);
			
			log.info("콘텐츠 완료 이벤트 발행: channel={}, studentId={}, contentId={}, weekId={}, courseId={}",
					CHANNEL, studentId, contentId, weekId, courseId);
			
		} catch (JsonProcessingException e) {
			log.error("이벤트 직렬화 실패: studentId={}, contentId={}", studentId, contentId, e);
		} catch (Exception e) {
			log.error("이벤트 발행 실패: studentId={}, contentId={}", studentId, contentId, e);
		}
	}
}
