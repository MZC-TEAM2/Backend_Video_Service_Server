package com.teambind.springproject.domain.stream.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 영상 시청 페이지 컨트롤러.
 * URL 경로로 contentId, videoId를 받고 userId는 쿼리 파라미터로 받는다.
 */
@Controller
public class WatchPageController {
	
	/**
	 * 영상 시청 페이지로 리다이렉트.
	 *
	 * @param contentId 콘텐츠 ID (week_contents.id)
	 * @param videoId   비디오 업로드 ID
	 * @param userId    사용자 ID
	 * @return watch-test.html로 리다이렉트
	 */
	@GetMapping("/watch/{contentId}/{videoId}")
	public RedirectView watchPage(
			@PathVariable final Long contentId,
			@PathVariable final Long videoId,
			@RequestParam final Long userId
	) {
		String redirectUrl = String.format(
				"/watch-test.html?userId=%d&contentId=%d&videoId=%d",
				userId, contentId, videoId
		);
		return new RedirectView(redirectUrl);
	}
}
