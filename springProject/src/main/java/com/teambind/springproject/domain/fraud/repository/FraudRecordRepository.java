package com.teambind.springproject.domain.fraud.repository;

import com.teambind.springproject.domain.fraud.entity.FraudRecord;
import com.teambind.springproject.domain.fraud.entity.FraudType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 부정 시청 기록 Repository.
 */
@Repository
public interface FraudRecordRepository extends JpaRepository<FraudRecord, Long> {

  /**
   * 세션의 부정 기록을 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 부정 기록 목록
   */
  List<FraudRecord> findBySessionId(Long sessionId);

  /**
   * 사용자의 콘텐츠별 부정 기록을 조회한다.
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @return 부정 기록 목록
   */
  List<FraudRecord> findByUserIdAndContentId(Long userId, Long contentId);

  /**
   * 사용자의 콘텐츠별 특정 유형 부정 기록을 조회한다.
   *
   * @param userId 사용자 ID
   * @param contentId 콘텐츠 ID
   * @param fraudType 부정 유형
   * @return 부정 기록 목록
   */
  List<FraudRecord> findByUserIdAndContentIdAndFraudType(
      Long userId,
      Long contentId,
      FraudType fraudType
  );

  /**
   * 세션의 부정 기록 수를 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 부정 기록 수
   */
  long countBySessionId(Long sessionId);
}
