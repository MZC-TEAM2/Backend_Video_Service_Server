package com.teambind.springproject.domain.upload.repository;

import com.teambind.springproject.domain.upload.entity.UploadStatus;
import com.teambind.springproject.domain.upload.entity.VideoUpload;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 비디오 업로드 Repository.
 */
@Repository
public interface VideoUploadRepository extends JpaRepository<VideoUpload, Long> {

  /**
   * TUS 업로드 ID로 조회한다.
   *
   * @param tusUploadId TUS 업로드 ID
   * @return VideoUpload (Optional)
   */
  Optional<VideoUpload> findByTusUploadId(String tusUploadId);

  /**
   * 사용자의 업로드 목록을 조회한다.
   *
   * @param userId 사용자 ID
   * @return 업로드 목록
   */
  List<VideoUpload> findByUserId(Long userId);

  /**
   * 사용자의 특정 상태 업로드 목록을 조회한다.
   *
   * @param userId 사용자 ID
   * @param status 상태
   * @return 업로드 목록
   */
  List<VideoUpload> findByUserIdAndStatus(Long userId, UploadStatus status);
}
