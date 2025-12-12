package com.teambind.springproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Keyspace Notification 설정 클래스.
 * TTL 만료 이벤트를 수신하기 위한 리스너 컨테이너를 설정한다.
 */
@Configuration
public class RedisKeyspaceConfig {

  private static final String EXPIRED_EVENT_PATTERN = "__keyevent@*__:expired";

  /**
   * Redis 메시지 리스너 컨테이너 빈 설정.
   *
   * @param connectionFactory Redis 연결 팩토리
   * @return RedisMessageListenerContainer 인스턴스
   */
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      final RedisConnectionFactory connectionFactory
  ) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    return container;
  }

  /**
   * 만료 이벤트 패턴 토픽.
   *
   * @return PatternTopic 인스턴스
   */
  @Bean
  public PatternTopic expiredEventTopic() {
    return new PatternTopic(EXPIRED_EVENT_PATTERN);
  }
}
