package com.teambind.springproject.domain.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 주차별 콘텐츠 엔티티.
 */
@Entity
@Table(
    name = "week_contents",
    indexes = {
        @Index(name = "idx_week_contents_week", columnList = "week_id"),
        @Index(name = "idx_week_contents_order", columnList = "display_order")
    }
)
public class WeekContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "course_id", nullable = false)
  private Long courseId;

  @Column(name = "week_id", nullable = false)
  private Long weekId;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false, length = 20)
  private ContentType contentType;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "content_url", nullable = false, length = 500)
  private String contentUrl;

  @Column(name = "duration", length = 10)
  private String duration;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected WeekContent() {
  }

  private WeekContent(
      final Long courseId,
      final Long weekId,
      final ContentType contentType,
      final String title,
      final String contentUrl,
      final String duration,
      final Integer displayOrder
  ) {
    this.courseId = courseId;
    this.weekId = weekId;
    this.contentType = contentType;
    this.title = title;
    this.contentUrl = contentUrl;
    this.duration = duration;
    this.displayOrder = displayOrder;
    this.createdAt = LocalDateTime.now();
  }

  /**
   * 비디오 콘텐츠를 생성한다.
   *
   * @param courseId 강의 ID
   * @param weekId 주차 ID
   * @param title 콘텐츠 제목
   * @param contentUrl 콘텐츠 URL
   * @param duration 동영상 길이 (예: "45:23")
   * @param displayOrder 표시 순서
   * @return WeekContent 인스턴스
   */
  public static WeekContent createVideo(
      final Long courseId,
      final Long weekId,
      final String title,
      final String contentUrl,
      final String duration,
      final Integer displayOrder
  ) {
    return new WeekContent(courseId, weekId, ContentType.VIDEO, title, contentUrl, duration,
        displayOrder);
  }

  /**
   * 문서 콘텐츠를 생성한다.
   *
   * @param courseId 강의 ID
   * @param weekId 주차 ID
   * @param title 콘텐츠 제목
   * @param contentUrl 콘텐츠 URL
   * @param displayOrder 표시 순서
   * @return WeekContent 인스턴스
   */
  public static WeekContent createDocument(
      final Long courseId,
      final Long weekId,
      final String title,
      final String contentUrl,
      final Integer displayOrder
  ) {
    return new WeekContent(courseId, weekId, ContentType.DOCUMENT, title, contentUrl, null,
        displayOrder);
  }

  /**
   * 링크 콘텐츠를 생성한다.
   *
   * @param courseId 강의 ID
   * @param weekId 주차 ID
   * @param title 콘텐츠 제목
   * @param contentUrl 콘텐츠 URL
   * @param displayOrder 표시 순서
   * @return WeekContent 인스턴스
   */
  public static WeekContent createLink(
      final Long courseId,
      final Long weekId,
      final String title,
      final String contentUrl,
      final Integer displayOrder
  ) {
    return new WeekContent(courseId, weekId, ContentType.LINK, title, contentUrl, null,
        displayOrder);
  }

  // Getters
  public Long getId() {
    return id;
  }

  public Long getCourseId() {
    return courseId;
  }

  public Long getWeekId() {
    return weekId;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getTitle() {
    return title;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public String getDuration() {
    return duration;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
