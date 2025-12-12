package com.teambind.springproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스.
 */
@Configuration
public class RedisConfig {

  /**
   * RedisTemplate 빈 설정.
   * Key는 String, Value는 JSON 직렬화를 사용한다.
   *
   * @param connectionFactory Redis 연결 팩토리
   * @return RedisTemplate 인스턴스
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      final RedisConnectionFactory connectionFactory
  ) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Key는 String 직렬화
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // Value는 JSON 직렬화
    GenericJackson2JsonRedisSerializer jsonSerializer =
        new GenericJackson2JsonRedisSerializer();
    template.setValueSerializer(jsonSerializer);
    template.setHashValueSerializer(jsonSerializer);

    template.afterPropertiesSet();
    return template;
  }
}
