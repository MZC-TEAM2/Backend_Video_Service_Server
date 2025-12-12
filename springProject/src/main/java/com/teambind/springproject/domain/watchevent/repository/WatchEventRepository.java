package com.teambind.springproject.domain.watchevent.repository;

import com.teambind.springproject.domain.watchevent.entity.WatchEvent;
import com.teambind.springproject.domain.watchevent.entity.WatchEventType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 시청 이벤트 Repository.
 */
@Repository
public interface WatchEventRepository extends JpaRepository<WatchEvent, Long> {

  /**
   * 세션 ID로 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 이벤트 목록
   */
  List<WatchEvent> findBySessionId(Long sessionId);

  /**
   * 사용자 ID와 콘텐츠 ID로 이벤트를 조회한다.
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 이벤트 목록
   */
  List<WatchEvent> findByUserIdAndContentId(Long userId, Long contentId);

  /**
   * 세션 ID와 이벤트 타입으로 이벤트를 조회한다.
   *
   * @param sessionId 세션 ID
   * @param eventType 이벤트 타입
   * @return 이벤트 목록
   */
  List<WatchEvent> findBySessionIdAndEventType(Long sessionId, WatchEventType eventType);

  /**
   * 특정 기간 내 사용자의 이벤트를 조회한다.
   *
   * @param userId 사용자 ID
   * @param startTime 시작 시간
   * @param endTime 종료 시간
   * @return 이벤트 목록
   */
  List<WatchEvent> findByUserIdAndEventTimestampBetween(
      Long userId,
      LocalDateTime startTime,
      LocalDateTime endTime
  );
}
