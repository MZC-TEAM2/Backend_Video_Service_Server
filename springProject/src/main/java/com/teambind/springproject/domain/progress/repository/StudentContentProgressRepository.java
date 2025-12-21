package com.teambind.springproject.domain.progress.repository;

import com.teambind.springproject.domain.progress.entity.StudentContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 학생 콘텐츠 진행 상황 Repository.
 */
@Repository
public interface StudentContentProgressRepository
		extends JpaRepository<StudentContentProgress, Long> {
	
	/**
	 * 콘텐츠 ID와 학생 ID로 진행 상황을 조회한다.
	 *
	 * @param contentId 콘텐츠 ID
	 * @param studentId 학생 ID
	 * @return 진행 상황 (Optional)
	 */
	Optional<StudentContentProgress> findByContentIdAndStudentId(Long contentId, Long studentId);
}
