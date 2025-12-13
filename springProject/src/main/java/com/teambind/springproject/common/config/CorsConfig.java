package com.teambind.springproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 설정 클래스.
 */
@Configuration
public class CorsConfig {
	
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		
		// 허용할 Origin
		config.addAllowedOrigin("http://localhost:5173"); // React Vite
		config.addAllowedOrigin("http://localhost:3000"); // React CRA
		
		// 허용할 HTTP 메서드
		config.addAllowedMethod("*");
		
		// 허용할 헤더
		config.addAllowedHeader("*");
		
		// 자격 증명 허용 (쿠키, 인증 헤더 등)
		config.setAllowCredentials(true);
		
		// 캐시 시간 (초)
		config.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		
		return new CorsFilter(source);
	}
}
