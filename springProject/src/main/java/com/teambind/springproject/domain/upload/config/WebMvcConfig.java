package com.teambind.springproject.domain.upload.config;

import com.teambind.springproject.domain.upload.event.TusUploadEventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final TusUploadEventListener tusUploadEventListener;

  public WebMvcConfig(final TusUploadEventListener tusUploadEventListener) {
    this.tusUploadEventListener = tusUploadEventListener;
  }

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(tusUploadEventListener)
        .addPathPatterns("/api/v1/videos/upload/**");
  }
}
