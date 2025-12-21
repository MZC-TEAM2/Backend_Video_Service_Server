package com.teambind.springproject.domain.session.event;

/**
 * 세션 타임아웃 이벤트.
 * Redis TTL 만료 시 발행된다.
 */
public record SessionTimeoutEvent(
		Long sessionId
) {
	
	/**
	 * Redis 키에서 세션 ID를 추출하여 이벤트를 생성한다.
	 *
	 * @param redisKey Redis 키 (예: watch:session:123)
	 * @return SessionTimeoutEvent 또는 null (키 형식이 맞지 않는 경우)
	 */
	public static SessionTimeoutEvent fromRedisKey(final String redisKey) {
		if (redisKey == null || !redisKey.startsWith("watch:session:")) {
			return null;
		}
		
		String sessionIdStr = redisKey.substring("watch:session:".length());
		try {
			Long sessionId = Long.parseLong(sessionIdStr);
			return new SessionTimeoutEvent(sessionId);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
