package com.teambind.springproject.domain.week.entity;

import jakarta.persistence.*;

/**
 * 강좌 주차 엔티티 (읽기 전용).
 * LMS DB의 course_weeks 테이블과 매핑.
 */
@Entity
@Table(name = "course_weeks")
public class CourseWeek {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "course_id", nullable = false)
	private Long courseId;
	
	@Column(name = "week_number", nullable = false)
	private Integer weekNumber;
	
	@Column(name = "title", length = 200)
	private String title;
	
	protected CourseWeek() {
	}
	
	// Getters
	public Long getId() {
		return id;
	}
	
	public Long getCourseId() {
		return courseId;
	}
	
	public Integer getWeekNumber() {
		return weekNumber;
	}
	
	public String getTitle() {
		return title;
	}
}
