package com.teambind.springproject.domain.session.entity;

/**
 * 시청 세션 상태를 나타내는 enum.
 */
public enum WatchSessionStatus {
	
	/**
	 * 활성 상태 - 현재 시청 중.
	 */
	ACTIVE("활성"),
	
	/**
	 * 종료됨 - 정상적으로 세션 종료.
	 */
	TERMINATED("종료"),
	
	/**
	 * 타임아웃 - 일정 시간 응답 없음으로 자동 종료.
	 */
	TIMEOUT("타임아웃"),
	
	/**
	 * 강제 종료 - 다른 콘텐츠 시청으로 인한 종료.
	 */
	REPLACED("대체됨");
	
	private final String description;
	
	WatchSessionStatus(final String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * 세션이 활성 상태인지 확인.
	 *
	 * @return 활성 상태이면 true
	 */
	public boolean isActive() {
		return this == ACTIVE;
	}
}
