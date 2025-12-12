package com.teambind.springproject.domain.stream.controller;

import com.teambind.springproject.domain.upload.entity.UploadStatus;
import com.teambind.springproject.domain.upload.entity.VideoUpload;
import com.teambind.springproject.domain.upload.repository.VideoUploadRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 비디오 스트리밍 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/videos/stream")
public class VideoStreamController {

  private static final Logger log = LoggerFactory.getLogger(VideoStreamController.class);
  private static final int BUFFER_SIZE = 8192;

  private final VideoUploadRepository uploadRepository;

  public VideoStreamController(final VideoUploadRepository uploadRepository) {
    this.uploadRepository = uploadRepository;
  }

  /**
   * 비디오를 스트리밍한다.
   *
   * @param videoId 비디오 업로드 ID
   * @param rangeHeader Range 헤더
   * @return 비디오 스트림
   */
  @GetMapping("/{videoId}")
  public ResponseEntity<StreamingResponseBody> streamVideo(
      @PathVariable final Long videoId,
      @RequestHeader(value = HttpHeaders.RANGE, required = false) final String rangeHeader
  ) {
    VideoUpload upload = uploadRepository.findById(videoId)
        .orElse(null);

    if (upload == null) {
      log.warn("비디오를 찾을 수 없음: videoId={}", videoId);
      return ResponseEntity.notFound().build();
    }

    if (upload.getStatus() != UploadStatus.COMPLETED) {
      log.warn("업로드가 완료되지 않음: videoId={}, status={}", videoId, upload.getStatus());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    Path videoPath = Paths.get(upload.getStoragePath());
    if (!Files.exists(videoPath)) {
      log.error("비디오 파일이 존재하지 않음: path={}", upload.getStoragePath());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    try {
      long fileSize = Files.size(videoPath);
      String contentType = upload.getContentType();
      if (contentType == null || contentType.isEmpty()) {
        contentType = "video/mp4";
      }

      if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
        return handleRangeRequest(videoPath, rangeHeader, fileSize, contentType);
      }

      return handleFullRequest(videoPath, fileSize, contentType);

    } catch (IOException e) {
      log.error("비디오 스트리밍 실패: videoId={}, error={}", videoId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private ResponseEntity<StreamingResponseBody> handleFullRequest(
      final Path videoPath,
      final long fileSize,
      final String contentType
  ) {
    StreamingResponseBody responseBody = outputStream -> {
      try (InputStream inputStream = Files.newInputStream(videoPath)) {
        copyStream(inputStream, outputStream, 0, fileSize);
      }
    };

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(contentType));
    headers.setContentLength(fileSize);
    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

    return ResponseEntity.ok()
        .headers(headers)
        .body(responseBody);
  }

  private ResponseEntity<StreamingResponseBody> handleRangeRequest(
      final Path videoPath,
      final String rangeHeader,
      final long fileSize,
      final String contentType
  ) {
    // bytes=0-1024 형태 파싱
    String range = rangeHeader.substring(6);
    String[] ranges = range.split("-");

    long rangeStart = Long.parseLong(ranges[0]);
    long rangeEnd = ranges.length > 1 && !ranges[1].isEmpty()
        ? Long.parseLong(ranges[1])
        : fileSize - 1;

    if (rangeEnd >= fileSize) {
      rangeEnd = fileSize - 1;
    }

    long contentLength = rangeEnd - rangeStart + 1;
    final long finalRangeStart = rangeStart;
    final long finalRangeEnd = rangeEnd;

    StreamingResponseBody responseBody = outputStream -> {
      try (InputStream inputStream = Files.newInputStream(videoPath)) {
        copyStream(inputStream, outputStream, finalRangeStart, contentLength);
      }
    };

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(contentType));
    headers.setContentLength(contentLength);
    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
    headers.set(HttpHeaders.CONTENT_RANGE,
        String.format("bytes %d-%d/%d", rangeStart, finalRangeEnd, fileSize));

    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
        .headers(headers)
        .body(responseBody);
  }

  private void copyStream(
      final InputStream inputStream,
      final OutputStream outputStream,
      final long skip,
      final long length
  ) throws IOException {
    if (skip > 0) {
      long skipped = inputStream.skip(skip);
      if (skipped < skip) {
        throw new IOException("스킵 실패: expected=" + skip + ", actual=" + skipped);
      }
    }

    byte[] buffer = new byte[BUFFER_SIZE];
    long remaining = length;

    while (remaining > 0) {
      int toRead = (int) Math.min(buffer.length, remaining);
      int read = inputStream.read(buffer, 0, toRead);
      if (read == -1) {
        break;
      }
      outputStream.write(buffer, 0, read);
      remaining -= read;
    }

    outputStream.flush();
  }
}
