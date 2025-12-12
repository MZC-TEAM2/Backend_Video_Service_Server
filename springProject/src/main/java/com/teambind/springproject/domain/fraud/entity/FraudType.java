package com.teambind.springproject.domain.fraud.entity;

/**
 * 부정 시청 유형.
 */
public enum FraudType {

  /**
   * 배속 재생 (1.0 초과).
   */
  FAST_PLAYBACK,

  /**
   * 탭 전환 (브라우저 비활성화).
   */
  TAB_HIDDEN,

  /**
   * 구간 스킵 (5초 초과).
   */
  SKIP_SEGMENT,

  /**
   * 동시 시청 (다른 세션).
   */
  CONCURRENT_SESSION
}
