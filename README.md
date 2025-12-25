# MZC 1st Backend Project - Video Service Server

LMS 연동 비디오 스트리밍 및 학습 진도 추적 서버

---

## Tech Stack

| Category        | Technology                      |
|-----------------|---------------------------------|
| Language        | Java 21                         |
| Framework       | Spring Boot 3.5.7               |
| Database        | MySQL 8.0                       |
| Cache           | Redis 7                         |
| Upload Protocol | TUS (tus-java-server 1.0.0-3.0) |
| Code Quality    | CheckStyle, PMD, SpotBugs       |
| Build Tool      | Gradle                          |
| Test            | JUnit 5, H2                     |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Client                                  │
│  (tus-js-client, Video Player, Progress Reporter)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Application                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Upload    │  │   Stream    │  │   Session   │              │
│  │  (TUS API)  │  │ (Range Req) │  │  (Tracking) │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Progress   │  │ WatchEvent  │  │    Fraud    │              │
│  │ (5s Report) │  │  (Logging)  │  │ (Detection) │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└───────────┬───────────────────────────────┬─────────────────────┘
            │                               │
            ▼                               ▼
┌─────────────────────┐         ┌─────────────────────┐
│     MySQL 8.0       │         │      Redis 7        │
│  • 영상 메타데이터    │         │  • 세션 TTL 관리     │
│  • 시청 기록 저장     │         │  • Keyspace 이벤트   │
│  • 진도 데이터       │         │  • 세션 타임아웃 감지  │
└─────────────────────┘         └─────────────────────┘
```

---

## Domains

| Domain         | Description                           |
|----------------|---------------------------------------|
| **Upload**     | TUS 프로토콜 기반 청크 업로드, 대용량 파일 지원, 재개 가능  |
| **Stream**     | HTTP Range Request 비디오 스트리밍, 구간 재생 지원 |
| **Session**    | 시청 세션 관리, 활성 세션 추적, 자동 종료             |
| **WatchEvent** | 시청 이벤트 기록 (재생, 일시정지, 탐색, 배속, 탭 전환)    |
| **Progress**   | 학습 진도 관리, 5초 주기 진행 상황 보고              |
| **Fraud**      | 부정 시청 감지 (스킵, 배속 재생, 탭 숨김)            |
| **Content**    | 주차별 콘텐츠 관리                            |
| **Learning**   | 학습률 계산 및 이벤트 발행                       |

---

## API Endpoints

### Upload (TUS Protocol)

| Method  | Endpoint                   | Description  |
|---------|----------------------------|--------------|
| OPTIONS | `/api/v1/videos/upload`    | TUS 서버 설정 조회 |
| POST    | `/api/v1/videos/upload`    | 업로드 생성       |
| HEAD    | `/api/v1/videos/upload/**` | 업로드 상태 조회    |
| PATCH   | `/api/v1/videos/upload/**` | 청크 업로드       |
| DELETE  | `/api/v1/videos/upload/**` | 업로드 취소       |

### Stream

| Method | Endpoint                          | Description         |
|--------|-----------------------------------|---------------------|
| GET    | `/api/v1/videos/stream/{videoId}` | 비디오 스트리밍 (Range 지원) |

### Session

| Method | Endpoint                       | Description |
|--------|--------------------------------|-------------|
| POST   | `/api/v1/sessions`             | 시청 세션 시작    |
| GET    | `/api/v1/sessions/{sessionId}` | 세션 정보 조회    |
| GET    | `/api/v1/sessions/active`      | 활성 세션 조회    |
| DELETE | `/api/v1/sessions/{sessionId}` | 세션 종료       |

### Watch Event

| Method | Endpoint                                        | Description |
|--------|-------------------------------------------------|-------------|
| POST   | `/api/v1/watch-events`                          | 시청 이벤트 기록   |
| GET    | `/api/v1/watch-events/session/{sessionId}`      | 세션별 이벤트 조회  |
| GET    | `/api/v1/watch-events/session/{sessionId}/type` | 타입별 이벤트 조회  |

### Progress

| Method | Endpoint                       | Description   |
|--------|--------------------------------|---------------|
| POST   | `/api/v1/progress`             | 진행 상황 보고      |
| GET    | `/api/v1/progress/{contentId}` | 콘텐츠별 진행 상황 조회 |

---

## Project Structure

```
springProject/
├── src/main/java/com/teambind/springproject/
│   ├── common/
│   │   ├── config/          # Redis, CORS, Custom 설정
│   │   ├── exceptions/      # 공통 예외 처리
│   │   └── util/            # 유틸리티 (Snowflake ID, JSON)
│   ├── domain/
│   │   ├── upload/          # TUS 업로드
│   │   │   ├── controller/  # TusUploadController
│   │   │   ├── service/     # UploadCompletionService
│   │   │   ├── entity/      # VideoUpload, UploadStatus
│   │   │   └── scheduler/   # UploadCleanupScheduler
│   │   ├── stream/          # 비디오 스트리밍
│   │   │   └── controller/  # VideoStreamController, WatchPageController
│   │   ├── session/         # 시청 세션
│   │   │   ├── controller/  # WatchSessionController
│   │   │   ├── service/     # WatchSessionService
│   │   │   ├── entity/      # WatchSession, WatchSessionStatus
│   │   │   ├── repository/  # RedisWatchSessionRepository
│   │   │   └── event/       # SessionTimeoutEvent, SessionExpiredListener
│   │   ├── watchevent/      # 시청 이벤트
│   │   │   ├── controller/  # WatchEventController
│   │   │   ├── service/     # WatchEventService
│   │   │   └── entity/      # WatchEvent, WatchEventType
│   │   ├── progress/        # 학습 진도
│   │   │   ├── controller/  # ProgressController
│   │   │   ├── service/     # ProgressService
│   │   │   ├── entity/      # StudentContentProgress
│   │   │   └── event/       # ContentCompletedEvent, ContentCompletedPublisher
│   │   ├── fraud/           # 부정 감지
│   │   │   ├── service/     # FraudDetectionService
│   │   │   └── entity/      # FraudRecord, FraudType, WatchedSegment
│   │   ├── content/         # 주차별 콘텐츠
│   │   ├── week/            # 강좌 주차
│   │   └── learning/        # 학습률
│   └── SpringProjectApplication.java
├── src/main/resources/
│   ├── application.yaml
│   ├── application-dev.yaml
│   └── static/              # 테스트 페이지 (upload-test, watch-test)
├── config/
│   ├── checkstyle/          # CheckStyle 설정
│   ├── pmd/                 # PMD 규칙
│   └── spotbugs/            # SpotBugs 필터
└── build.gradle
```

---

## Key Features

### TUS Protocol Upload

- 청크 단위 업로드로 대용량 파일 지원
- 네트워크 중단 시 재개 가능
- 업로드 완료 시 WeekContent 자동 생성

### HTTP Range Request Streaming

- 구간 재생 지원 (Partial Content 206)
- 효율적인 대역폭 사용
- 다양한 비디오 포맷 지원

### Watch Session Management

- 사용자별 활성 세션 관리
- Redis TTL 기반 세션 타임아웃
- Keyspace 이벤트를 통한 만료 감지

### Fraud Detection

- 스킵 감지 (5초 이상 건너뛰기)
- 배속 재생 감지 (1.0x 초과)
- 탭 숨김 감지 (Visibility API)
- 유효/무효 시청 구간 분리 기록

### Progress Tracking

- 5초 주기 진행 상황 보고
- 콘텐츠 완료 이벤트 발행 (Redis Pub/Sub)
- LMS 서버 연동을 위한 이벤트 시스템

---

## Access Points

| Service     | URL                                    |
|-------------|----------------------------------------|
| API Server  | http://localhost:8090                  |
| Upload Test | http://localhost:8090/upload-test.html |
| Watch Test  | http://localhost:8090/watch-test.html  |

---
