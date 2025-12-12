package com.teambind.springproject.domain.session.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis 키 만료 이벤트 리스너.
 * 세션 키가 만료되면 SessionTimeoutEvent를 발행한다.
 */
@Component
public class SessionExpiredListener implements MessageListener {

  private static final Logger log = LoggerFactory.getLogger(SessionExpiredListener.class);
  private static final String SESSION_KEY_PREFIX = "watch:session:";

  private final ApplicationEventPublisher eventPublisher;

  /**
   * 생성자.
   *
   * @param listenerContainer Redis 메시지 리스너 컨테이너
   * @param expiredEventTopic 만료 이벤트 토픽
   * @param eventPublisher Spring 이벤트 발행자
   */
  public SessionExpiredListener(
      final RedisMessageListenerContainer listenerContainer,
      final PatternTopic expiredEventTopic,
      final ApplicationEventPublisher eventPublisher
  ) {
    this.eventPublisher = eventPublisher;
    listenerContainer.addMessageListener(this, expiredEventTopic);
  }

  @Override
  public void onMessage(final Message message, final byte[] pattern) {
    String expiredKey = new String(message.getBody());

    if (!expiredKey.startsWith(SESSION_KEY_PREFIX)) {
      return;
    }

    SessionTimeoutEvent event = SessionTimeoutEvent.fromRedisKey(expiredKey);
    if (event != null) {
      log.info("세션 타임아웃 감지: sessionId={}", event.sessionId());
      eventPublisher.publishEvent(event);
    }
  }
}
