package com.teambind.springproject.domain.progress.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 시청 진행 상황 보고 요청 DTO.
 * sessionId는 JavaScript Number 정밀도 문제로 String으로 받는다.
 */
public record ProgressReportRequest(
		@NotBlank(message = "세션 ID는 필수입니다")
		String sessionId,
		
		@NotNull(message = "콘텐츠 ID는 필수입니다")
		Long contentId,
		
		@NotNull(message = "현재 위치는 필수입니다")
		@Min(value = 0, message = "현재 위치는 0 이상이어야 합니다")
		Integer currentPositionSeconds,
		
		@NotNull(message = "전체 길이는 필수입니다")
		@Min(value = 1, message = "전체 길이는 1 이상이어야 합니다")
		Integer totalDurationSeconds
) {
	
	/**
	 * sessionId를 Long으로 변환한다.
	 */
	public Long getSessionIdAsLong() {
		return Long.parseLong(sessionId);
	}
}
