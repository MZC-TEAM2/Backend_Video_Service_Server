package com.teambind.springproject.domain.upload.controller;

import com.teambind.springproject.domain.upload.service.UploadCompletionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * TUS 프로토콜 기반 청크 업로드 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/videos/upload")
@CrossOrigin(
		exposedHeaders = {
				"Location", "Upload-Offset", "Upload-Length",
				"Tus-Version", "Tus-Resumable", "Tus-Max-Size", "Tus-Extension",
				"X-Watch-URL", "X-Video-Id", "X-Content-Id"
		}
)
public class TusUploadController {

	private static final Logger log = LoggerFactory.getLogger(TusUploadController.class);

	private final TusFileUploadService tusService;
	private final UploadCompletionService completionService;

	public TusUploadController(
			final TusFileUploadService tusService,
			final UploadCompletionService completionService
	) {
		this.tusService = tusService;
		this.completionService = completionService;
	}

	/**
	 * TUS OPTIONS 요청 처리.
	 * 클라이언트가 서버의 TUS 지원 여부와 확장 기능을 확인한다.
	 */
	@RequestMapping(method = RequestMethod.OPTIONS)
	public void processOptions(
			final HttpServletRequest request,
			final HttpServletResponse response
	) throws IOException {
		tusService.process(request, response);
		log.debug("TUS OPTIONS 요청 처리");
	}

	/**
	 * TUS POST 요청 처리.
	 * 새로운 업로드를 생성하고 week_contents를 미리 생성한다.
	 */
	@PostMapping
	public void processPost(
			final HttpServletRequest request,
			final HttpServletResponse response,
			@RequestHeader(value = "Upload-Length", required = false) final Long uploadLength
	) throws IOException {
		// TUS 처리
		tusService.process(request, response);

		// 201 Created 응답인 경우에만 week_contents 생성
		if (response.getStatus() == 201) {
			try {
				String location = response.getHeader("Location");
				if (location != null && !location.isEmpty()) {
					// 경로 추출
					String uploadUri = location;
					if (location.contains("://")) {
						int pathStart = location.indexOf("/", location.indexOf("://") + 3);
						if (pathStart > 0) {
							uploadUri = location.substring(pathStart);
						}
					}

					// 메타데이터 저장 및 week_contents 생성
					UploadCompletionService.UploadResult result =
							completionService.saveUploadMetadataAndCreateContent(uploadUri, 1L);

					// 응답 헤더에 watchUrl 추가
					if (result.getWatchUrl() != null) {
						response.setHeader("X-Watch-URL", result.getWatchUrl());
						response.setHeader("X-Video-Id", String.valueOf(result.getVideoId()));
						response.setHeader("X-Content-Id", String.valueOf(result.getContentId()));
						log.info("업로드 생성 완료: watchUrl={}", result.getWatchUrl());
					}
				}
			} catch (Exception e) {
				log.error("week_contents 생성 실패: {}", e.getMessage(), e);
			}
		}

		log.info("TUS POST 요청 처리: Upload-Length={}", uploadLength);
	}

	/**
	 * TUS HEAD 요청 처리.
	 * 업로드 상태를 조회한다.
	 */
	@RequestMapping(value = "/**", method = RequestMethod.HEAD)
	public void processHead(
			final HttpServletRequest request,
			final HttpServletResponse response
	) throws IOException {
		tusService.process(request, response);
		log.debug("TUS HEAD 요청 처리: {}", request.getRequestURI());
	}

	/**
	 * TUS PATCH 요청 처리.
	 * 청크 데이터를 업로드한다.
	 */
	@PatchMapping("/**")
	public void processPatch(
			final HttpServletRequest request,
			final HttpServletResponse response,
			@RequestHeader(value = "Upload-Offset", required = false) final Long uploadOffset
	) throws IOException {
		tusService.process(request, response);

		// 업로드 완료 체크 및 처리
		if (response.getStatus() == 204) {
			try {
				String uri = request.getRequestURI();
				completionService.handlePatchCompletion(uri);
			} catch (Exception e) {
				log.error("업로드 진행 처리 실패: {}", e.getMessage(), e);
			}
		}

		log.debug("TUS PATCH 요청 처리: Upload-Offset={}", uploadOffset);
	}

	/**
	 * TUS DELETE 요청 처리.
	 * 업로드를 취소한다.
	 */
	@DeleteMapping("/**")
	public void processDelete(
			final HttpServletRequest request,
			final HttpServletResponse response
	) throws IOException {
		tusService.process(request, response);
		log.info("TUS DELETE 요청 처리: {}", request.getRequestURI());
	}
}
