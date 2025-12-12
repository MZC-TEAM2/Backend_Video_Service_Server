package com.teambind.springproject.domain.watchevent.entity;

/**
 * 시청 이벤트 타입.
 */
public enum WatchEventType {

  /**
   * 재생 시작.
   */
  PLAY,

  /**
   * 일시 정지.
   */
  PAUSE,

  /**
   * 위치 이동 (구간 건너뛰기).
   */
  SEEK,

  /**
   * 재생 속도 변경.
   */
  RATE_CHANGE,

  /**
   * 브라우저 탭 숨김 (비활성화).
   */
  VISIBILITY_HIDDEN,

  /**
   * 브라우저 탭 표시 (활성화).
   */
  VISIBILITY_VISIBLE
}
