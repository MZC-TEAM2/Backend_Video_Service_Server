package com.teambind.springproject.domain.upload.controller;

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
				"Tus-Version", "Tus-Resumable", "Tus-Max-Size", "Tus-Extension"
		}
)
public class TusUploadController {
	
	private static final Logger log = LoggerFactory.getLogger(TusUploadController.class);
	
	private final TusFileUploadService tusService;
	
	public TusUploadController(final TusFileUploadService tusService) {
		this.tusService = tusService;
	}
	
	/**
	 * TUS OPTIONS 요청 처리.
	 * 클라이언트가 서버의 TUS 지원 여부와 확장 기능을 확인한다.
	 *
	 * @param request  HTTP 요청
	 * @param response HTTP 응답
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
	 * 새로운 업로드를 생성한다.
	 *
	 * @param request      HTTP 요청
	 * @param response     HTTP 응답
	 * @param uploadLength 업로드 파일 크기
	 */
	@PostMapping
	public void processPost(
			final HttpServletRequest request,
			final HttpServletResponse response,
			@RequestHeader(value = "Upload-Length", required = false) final Long uploadLength
	) throws IOException {
		tusService.process(request, response);
		log.info("TUS POST 요청 처리: Upload-Length={}", uploadLength);
	}
	
	/**
	 * TUS HEAD 요청 처리.
	 * 업로드 상태를 조회한다.
	 *
	 * @param request  HTTP 요청
	 * @param response HTTP 응답
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
	 *
	 * @param request      HTTP 요청
	 * @param response     HTTP 응답
	 * @param uploadOffset 현재 업로드 오프셋
	 */
	@PatchMapping("/**")
	public void processPatch(
			final HttpServletRequest request,
			final HttpServletResponse response,
			@RequestHeader(value = "Upload-Offset", required = false) final Long uploadOffset
	) throws IOException {
		tusService.process(request, response);
		log.debug("TUS PATCH 요청 처리: Upload-Offset={}", uploadOffset);
	}
	
	/**
	 * TUS DELETE 요청 처리.
	 * 업로드를 취소한다.
	 *
	 * @param request  HTTP 요청
	 * @param response HTTP 응답
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
