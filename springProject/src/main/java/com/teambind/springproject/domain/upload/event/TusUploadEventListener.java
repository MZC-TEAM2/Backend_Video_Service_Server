package com.teambind.springproject.domain.upload.event;

import com.teambind.springproject.domain.upload.service.UploadCompletionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.upload.UploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TUS 업로드 이벤트 인터셉터.
 * 업로드 완료 시 자동으로 파일을 최종 저장소로 이동한다.
 */
@Component
public class TusUploadEventListener implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(TusUploadEventListener.class);

  private final TusFileUploadService tusService;
  private final UploadCompletionService completionService;

  public TusUploadEventListener(
      final TusFileUploadService tusService,
      final UploadCompletionService completionService
  ) {
    this.tusService = tusService;
    this.completionService = completionService;
  }

  @Override
  public void afterCompletion(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      final Exception ex
  ) {
    String method = request.getMethod();
    String uri = request.getRequestURI();

    if (!uri.startsWith("/api/v1/videos/upload")) {
      return;
    }

    try {
      if ("PATCH".equals(method)) {
        handlePatchCompletion(uri, response);
      }
    } catch (Exception e) {
      log.error("업로드 이벤트 처리 실패: {}", e.getMessage(), e);
    }
  }

  private void handlePatchCompletion(final String uri, final HttpServletResponse response)
      throws Exception {
    // 응답 성공 시에만 처리
    if (response.getStatus() != 204) {
      return;
    }

    UploadInfo uploadInfo = tusService.getUploadInfo(uri);

    if (uploadInfo == null) {
      return;
    }

    // 업로드 완료 여부 확인
    if (!uploadInfo.isUploadInProgress()
        && uploadInfo.getOffset().equals(uploadInfo.getLength())) {
      log.info("업로드 완료 감지: uri={}, size={}", uri, uploadInfo.getLength());

      try {
        String storagePath = completionService.completeUpload(uri);
        log.info("파일 저장 완료: {}", storagePath);
      } catch (Exception e) {
        log.error("업로드 완료 처리 실패: {}", e.getMessage(), e);
      }
    } else {
      // 진행 상황 업데이트
      completionService.updateUploadProgress(uri);
    }
  }
}
