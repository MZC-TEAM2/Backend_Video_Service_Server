package com.teambind.springproject.domain.progress.controller;

import com.teambind.springproject.domain.progress.dto.ProgressReportRequest;
import com.teambind.springproject.domain.progress.dto.ProgressResponse;
import com.teambind.springproject.domain.progress.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 시청 진행 상황 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {
	
	private final ProgressService progressService;
	
	public ProgressController(final ProgressService progressService) {
		this.progressService = progressService;
	}
	
	/**
	 * 시청 진행 상황을 보고한다.
	 * 클라이언트에서 주기적으로(5초) 호출한다.
	 *
	 * @param request 진행 상황 보고 요청
	 * @return 업데이트된 진행 상황
	 */
	@PostMapping
	public ResponseEntity<ProgressResponse> reportProgress(
			@Valid @RequestBody final ProgressReportRequest request
	) {
		ProgressResponse response = progressService.reportProgress(request);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 콘텐츠별 진행 상황을 조회한다.
	 *
	 * @param contentId 콘텐츠 ID
	 * @param studentId 학생 ID
	 * @return 진행 상황
	 */
	@GetMapping("/{contentId}")
	public ResponseEntity<ProgressResponse> getProgress(
			@PathVariable final Long contentId,
			@RequestParam final Long studentId
	) {
		Optional<ProgressResponse> response = progressService.getProgress(contentId, studentId);
		return response
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
