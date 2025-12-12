package com.teambind.springproject.domain.progress.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 시청 진행 상황 보고 요청 DTO.
 */
public record ProgressReportRequest(
    @NotNull(message = "세션 ID는 필수입니다")
    Long sessionId,

    @NotNull(message = "콘텐츠 ID는 필수입니다")
    Long contentId,

    @NotNull(message = "현재 위치는 필수입니다")
    @Min(value = 0, message = "현재 위치는 0 이상이어야 합니다")
    Integer currentPositionSeconds,

    @NotNull(message = "전체 길이는 필수입니다")
    @Min(value = 1, message = "전체 길이는 1 이상이어야 합니다")
    Integer totalDurationSeconds
) {
}
