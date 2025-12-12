package com.teambind.springproject.domain.session.repository;

import com.teambind.springproject.domain.session.entity.WatchSession;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis 기반 시청 세션 저장소 구현체.
 */
@Repository
public class RedisWatchSessionRepository implements WatchSessionRepository {

  private static final String SESSION_KEY_PREFIX = "watch:session:";
  private static final String SESSION_BACKUP_KEY_PREFIX = "watch:session:backup:";
  private static final String USER_ACTIVE_SESSION_KEY_PREFIX = "watch:user:active:";
  private static final long DEFAULT_TTL_SECONDS = 30;
  private static final long BACKUP_TTL_SECONDS = 60;

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisWatchSessionRepository(final RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void save(final WatchSession session) {
    String sessionKey = getSessionKey(session.getSessionId());
    String backupKey = getBackupKey(session.getSessionId());
    String userActiveKey = getUserActiveSessionKey(session.getUserId());

    redisTemplate.opsForValue().set(sessionKey, session, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);

    // 타임아웃 처리를 위한 백업 저장 (TTL보다 긴 시간 동안 유지)
    redisTemplate.opsForValue().set(backupKey, session, BACKUP_TTL_SECONDS, TimeUnit.SECONDS);

    if (session.isActive()) {
      redisTemplate.opsForValue().set(
          userActiveKey,
          session.getSessionId(),
          DEFAULT_TTL_SECONDS,
          TimeUnit.SECONDS
      );
    }
  }

  @Override
  public Optional<WatchSession> findById(final Long sessionId) {
    String key = getSessionKey(sessionId);
    Object value = redisTemplate.opsForValue().get(key);
    if (value instanceof WatchSession) {
      return Optional.of((WatchSession) value);
    }
    return Optional.empty();
  }

  @Override
  public Optional<WatchSession> findActiveByUserId(final Long userId) {
    String userActiveKey = getUserActiveSessionKey(userId);
    Object sessionIdObj = redisTemplate.opsForValue().get(userActiveKey);

    if (sessionIdObj instanceof Long sessionId) {
      return findById(sessionId);
    }
    return Optional.empty();
  }

  @Override
  public void delete(final Long sessionId) {
    String key = getSessionKey(sessionId);
    redisTemplate.delete(key);
  }

  @Override
  public void deleteActiveByUserId(final Long userId) {
    String userActiveKey = getUserActiveSessionKey(userId);
    redisTemplate.delete(userActiveKey);
  }

  @Override
  public void refreshTtl(final Long sessionId, final long ttlSeconds) {
    String key = getSessionKey(sessionId);
    redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
  }

  @Override
  public Optional<WatchSession> findBackupById(final Long sessionId) {
    String backupKey = getBackupKey(sessionId);
    Object value = redisTemplate.opsForValue().get(backupKey);
    if (value instanceof WatchSession) {
      return Optional.of((WatchSession) value);
    }
    return Optional.empty();
  }

  @Override
  public void deleteBackup(final Long sessionId) {
    String backupKey = getBackupKey(sessionId);
    redisTemplate.delete(backupKey);
  }

  private String getSessionKey(final Long sessionId) {
    return SESSION_KEY_PREFIX + sessionId;
  }

  private String getBackupKey(final Long sessionId) {
    return SESSION_BACKUP_KEY_PREFIX + sessionId;
  }

  private String getUserActiveSessionKey(final Long userId) {
    return USER_ACTIVE_SESSION_KEY_PREFIX + userId;
  }
}
