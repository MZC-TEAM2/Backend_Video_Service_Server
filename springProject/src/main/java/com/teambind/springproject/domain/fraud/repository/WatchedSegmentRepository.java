package com.teambind.springproject.domain.fraud.repository;

import com.teambind.springproject.domain.fraud.entity.WatchedSegment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 시청 구간 Repository.
 */
@Repository
public interface WatchedSegmentRepository extends JpaRepository<WatchedSegment, Long> {

  /**
   * 세션의 모든 시청 구간을 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 시청 구간 목록
   */
  List<WatchedSegment> findBySessionId(Long sessionId);

  /**
   * 사용자의 콘텐츠별 유효한 시청 구간을 조회한다.
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 유효한 시청 구간 목록
   */
  List<WatchedSegment> findByUserIdAndContentIdAndIsValidTrue(Long userId, Long contentId);

  /**
   * 사용자의 콘텐츠별 총 유효 시청 시간을 계산한다.
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 총 유효 시청 시간(초)
   */
  @Query("SELECT COALESCE(SUM(s.endSeconds - s.startSeconds), 0) "
      + "FROM WatchedSegment s "
      + "WHERE s.userId = :userId AND s.contentId = :contentId AND s.isValid = true")
  Integer sumValidWatchedSeconds(
      @Param("userId") Long userId,
      @Param("contentId") Long contentId
  );
}
