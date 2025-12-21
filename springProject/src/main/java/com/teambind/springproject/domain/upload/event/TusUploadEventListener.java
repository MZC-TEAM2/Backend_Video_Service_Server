package com.teambind.springproject.domain.upload.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TUS 업로드 이벤트 인터셉터.
 * 현재는 로깅 용도로만 사용 (실제 처리는 TusUploadController에서 수행).
 */
@Component
public class TusUploadEventListener implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(TusUploadEventListener.class);

	public TusUploadEventListener() {
	}
}
