package com.teambind.springproject.domain.session.repository;

import com.teambind.springproject.domain.session.entity.WatchSession;
import java.util.Optional;

/**
 * 시청 세션 저장소 인터페이스.
 * Redis 기반 구현체에서 활성 세션을 관리한다.
 */
public interface WatchSessionRepository {

  /**
   * 세션을 저장한다.
   *
   * @param session 저장할 세션
   */
  void save(WatchSession session);

  /**
   * 세션 ID로 세션을 조회한다.
   *
   * @param sessionId 세션 ID
   * @return 세션 (Optional)
   */
  Optional<WatchSession> findById(Long sessionId);

  /**
   * 사용자의 현재 활성 세션을 조회한다.
   *
   * @param userId 사용자 ID
   * @return 활성 세션 (Optional)
   */
  Optional<WatchSession> findActiveByUserId(Long userId);

  /**
   * 세션을 삭제한다.
   *
   * @param sessionId 세션 ID
   */
  void delete(Long sessionId);

  /**
   * 사용자의 활성 세션을 삭제한다.
   *
   * @param userId 사용자 ID
   */
  void deleteActiveByUserId(Long userId);

  /**
   * 세션 TTL을 갱신한다.
   *
   * @param sessionId 세션 ID
   * @param ttlSeconds TTL (초)
   */
  void refreshTtl(Long sessionId, long ttlSeconds);
}
