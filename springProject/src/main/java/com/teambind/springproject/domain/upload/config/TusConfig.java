package com.teambind.springproject.domain.upload.config;

import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TUS 프로토콜 기반 업로드 설정.
 */
@Configuration
public class TusConfig {
	
	@Value("${video.upload.temp-path:/tmp/video-temp}")
	private String tempPath;
	
	@Value("${video.upload.chunk-size:5242880}")
	private Long chunkSize;
	
	@Value("${video.upload.max-file-size:10737418240}")
	private Long maxFileSize;
	
	/**
	 * TUS 파일 업로드 서비스 빈 설정.
	 *
	 * @return TusFileUploadService 인스턴스
	 */
	@Bean
	public TusFileUploadService tusFileUploadService() {
		return new TusFileUploadService()
				.withStoragePath(tempPath)
				.withDownloadFeature()
				.withUploadExpirationPeriod(24 * 60 * 60 * 1000L)  // 24시간
				.withMaxUploadSize(maxFileSize)
				.withThreadLocalCache(true)
				.withUploadUri("/api/v1/videos/upload");
	}
}
