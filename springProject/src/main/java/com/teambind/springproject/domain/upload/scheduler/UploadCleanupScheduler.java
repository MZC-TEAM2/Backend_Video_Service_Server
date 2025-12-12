package com.teambind.springproject.domain.upload.scheduler;

import java.io.IOException;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 업로드 임시 파일 정리 스케줄러.
 * 만료된 업로드 임시 파일을 주기적으로 정리한다.
 */
@Component
public class UploadCleanupScheduler {

  private static final Logger log = LoggerFactory.getLogger(UploadCleanupScheduler.class);

  private final TusFileUploadService tusService;

  public UploadCleanupScheduler(final TusFileUploadService tusService) {
    this.tusService = tusService;
  }

  /**
   * 만료된 업로드를 정리한다.
   * 매일 새벽 3시에 실행.
   */
  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupExpiredUploads() {
    log.info("만료된 업로드 정리 시작");

    try {
      tusService.cleanup();
      log.info("만료된 업로드 정리 완료");
    } catch (IOException e) {
      log.error("업로드 정리 실패: {}", e.getMessage(), e);
    }
  }
}
