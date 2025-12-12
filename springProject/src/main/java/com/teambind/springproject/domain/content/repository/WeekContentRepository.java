package com.teambind.springproject.domain.content.repository;

import com.teambind.springproject.domain.content.entity.WeekContent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 주차별 콘텐츠 리포지토리.
 */
public interface WeekContentRepository extends JpaRepository<WeekContent, Long> {

  /**
   * 주차별 최대 표시 순서를 조회한다.
   *
   * @param weekId 주차 ID
   * @return 최대 표시 순서
   */
  @Query("SELECT COALESCE(MAX(wc.displayOrder), 0) FROM WeekContent wc WHERE wc.weekId = :weekId")
  Integer findMaxDisplayOrderByWeekId(@Param("weekId") Long weekId);

  /**
   * 콘텐츠 URL로 콘텐츠를 조회한다.
   *
   * @param contentUrl 콘텐츠 URL
   * @return WeekContent
   */
  Optional<WeekContent> findByContentUrl(String contentUrl);
}
