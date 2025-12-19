package com.teambind.springproject.domain.week.repository;

import com.teambind.springproject.domain.week.entity.CourseWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 강좌 주차 리포지토리 (읽기 전용).
 */
public interface CourseWeekRepository extends JpaRepository<CourseWeek, Long> {

	/**
	 * 주차 ID로 강좌 주차를 조회한다.
	 *
	 * @param id 주차 ID
	 * @return CourseWeek
	 */
	Optional<CourseWeek> findById(Long id);
}
