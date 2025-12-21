package com.teambind.springproject.domain.upload.entity;

/**
 * 업로드 상태.
 */
public enum UploadStatus {
	
	/**
	 * 업로드 진행 중.
	 */
	IN_PROGRESS,
	
	/**
	 * 업로드 완료.
	 */
	COMPLETED,
	
	/**
	 * 업로드 취소.
	 */
	CANCELLED,
	
	/**
	 * 업로드 실패.
	 */
	FAILED,
	
	/**
	 * 업로드 만료.
	 */
	EXPIRED
}
