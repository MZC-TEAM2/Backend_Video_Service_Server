package com.teambind.springproject.domain.session.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 시청 세션 시작 요청 DTO.
 */
public record SessionStartRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    Long userId,

    @NotNull(message = "콘텐츠 ID는 필수입니다")
    Long contentId
) {
}
