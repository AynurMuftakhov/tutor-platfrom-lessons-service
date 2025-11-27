# Lessons Service

Lessons Service is a Spring Boot microservice that orchestrates the lesson lifecycle for speakshire.com. It keeps the source of truth for lessons, materials, grammar items, and listening tasks while delegating AI-assisted authoring to OpenAI and speech synthesis to ElevenLabs. The service exposes REST APIs for tutor and student applications, persists state in PostgreSQL, emits lesson events to Kafka, and stores supporting media on the local filesystem.

## Feature Highlights
- **Lesson lifecycle management** – Create single or recurring lessons, filter schedules by tutor/student/status, fetch “now/upcoming” lessons, compute monthly counts, and expose on-platform tutor statistics (`LessonController`, `StatisticsService`).
- **Learning materials & grammar** – Manage folders, rich media materials, grammar items, and tag-based search. Materials can be linked to lessons, reordered, or grouped by custom folders (`MaterialController`, `MaterialFoldersController`, `LessonMaterialController`).
- **Listening comprehension workflows** – Build listening tasks per lesson or material, track readiness states, and attach AI-generated audio via asynchronous jobs guarded by idempotency keys. Teachers can generate, edit, or validate transcripts against vocabulary objectives using Lucene-powered coverage analysis (`ListeningTaskController`, `ListeningAudioController`, `ListeningTranscriptController`, `EnglishCoverageEngine`).
- **AI authoring toolset** – Generate HTML lesson text blocks and cloze-style grammar exercises via OpenAI. The service hardens prompts, normalizes HTML, retries JSON-only responses, and records metadata for downstream tooling (`TextGenerationService`, `ExerciseAiService`, `OpenAiClient`).
- **Notes, clips, and uploads** – Teachers can keep rich lesson notes, upload lesson clips (WebM/OGG/WAV) with TTL-based cleanup, and manage image/audio assets stored under `/uploads/**` with static resource mappings and per-endpoint CORS (`LessonNotesController`, `ClipController`, `ImageService`, `LocalImageStorageService`, `LocalAudioStorageService`).
- **Operational guardrails** – Global exception handlers standardize API responses, Micrometer + Actuator expose health/metrics (Prometheus-ready), and scheduled jobs purge expired clips or audio (`GlobalExceptionHandler`, `UploadsExceptionHandler`, `ClipCleanupScheduler`).

## Architecture
- **Tech stack** – Java 17, Spring Boot 3.4, Spring Web/MVC, Spring Data JPA, Spring Kafka, Spring Validation, Spring Scheduling, Micrometer + Prometheus registry, MapStruct, and Lombok.
- **Data & messaging** – PostgreSQL stores lessons, materials, transcripts, and audio job metadata. Kafka publishers (`LessonEventProducer`) broadcast reschedule events. Lucene analyzers back coverage calculations. Filesystem storage (configurable paths) keeps uploaded images, generated audio, and transient lesson clips.
- **Integration clients** – `OpenAiClient` (WebClient) for text/transcript generation, `ExerciseAiService` (RestTemplate) for GPT‑4o exercise generation, `VocabularyClient` for the vocabulary-service REST API, and `ElevenLabsClient` (Java 11 `HttpClient`) for TTS. Each client enforces timeouts, retries, and structured logging with request IDs.
- **Modular layout** – Controllers expose cohesive API domains (lessons, materials, AI, listening, uploads). Services encapsulate orchestration and validation, repositories wrap Spring Data JPA, and mappers provide API/domain translation. Static resource configs serve uploaded files directly via the app.

## External Dependencies
| Dependency | Purpose |
| --- | --- |
| PostgreSQL (`SPRING_DATASOURCE_URL`) | Primary relational data store for lessons, materials, transcripts, notes, and jobs. |
| Kafka (`KAFKA_BOOTSTRAP_SERVERS`) | Optional event emission when lessons are rescheduled. |
| OpenAI (`OPENAI_API_KEY`, `openai.api.*`) | Text block, transcript, and exercise generation. |
| ElevenLabs (`ELEVENLABS_API_KEY`, `ELEVENLABS_VOICE_ID`) | Text-to-speech audio for listening tasks. |
| Vocabulary Service (`VOCABULARY_SERVICE_URL`) | Resolves vocabulary IDs to enforce transcript coverage. |

## Configuration
All configuration is driven by Spring properties or environment variables (see `lessons.env` for a sample). Key settings include:

| Property / Env | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | PostgreSQL connection details. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Defaults to `update`; adjust in production migrations. |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker list for `LessonEventProducer`. |
| `OPENAI_API_KEY`, `openai.api.url`, `openai.model` | OpenAI auth and model selection. |
| `ELEVENLABS_API_KEY`, `ELEVENLABS_VOICE_ID`, `elevenlabs.*` | ElevenLabs auth, default voice, retries, and formats. |
| `VOCABULARY_SERVICE_URL`, `VOCABULARY_WORDS_PATH` | Downstream vocabulary service endpoints. |
| `uploads.dir`, `uploads.audio.dir` | Base directories for image and audio assets served via `/uploads/**`. |
| `clips.*` | Clip storage root, TTL, size limit, allowed MIME types, and dedicated CORS options. |
| `listening.audio.*` | Audio job TTL, worker pool size, and idempotency enforcement. |
| `spring.security.user.*` | Built-in basic auth fallback; override or disable behind API gateways. |

## Running Locally
1. **Prerequisites** – Java 17, Maven 3.9+, Docker (optional), running PostgreSQL (default `lessons_service` DB), and Kafka if you want event publishing. Obtain valid OpenAI and ElevenLabs API keys.
2. **Environment** – Copy `lessons.env` to `.env` or export the variables manually. Update secrets before running.
3. **Database** – Create the target database and user referenced in your env vars. JPA will auto-create tables via `ddl-auto=update`.
4. **Build** – `./mvnw clean package` to compile, run unit tests, and create `target/lessons-service-0.0.1-SNAPSHOT.jar`.
5. **Run** – `./mvnw spring-boot:run` (uses the local classpath) or `java -jar target/lessons-service-0.0.1-SNAPSHOT.jar`.
6. **Docker** – After packaging, `docker build -t lessons-service .` then run with the necessary env variables mounted (`docker run --env-file lessons.env -p 8082:8082 lessons-service`).

## API Surface at a Glance
| Area | Sample Endpoints | Notes |
| --- | --- | --- |
| Lessons & schedules | `POST /api/lessons`, `GET /api/lessons/upcoming`, `GET /api/lessons/now` | Supports recurring series, filters, statistics, and deletions. |
| Lesson content & materials | `POST /api/lesson-contents`, `GET /api/materials`, `PATCH /api/lessons/{lessonId}/materials/{linkId}` | Manage authored content, folders, and lesson/material linking with ordering. |
| Listening workflows | `POST /api/listening/audio/generate`, `GET /api/listening/transcripts/{id}`, `POST /api/listening/transcripts/validate` | Combine transcript generation/validation, audio jobs, and voice catalog queries. |
| AI authoring | `POST /api/ai/text-blocks`, `POST /api/ai/exercises` | Generates sanitized HTML text and JSON grammar exercises with teacher metadata. |
| Notes & uploads | `PUT /api/lessons/{lessonId}/notes`, `POST /api/uploads/images`, `POST /api/lessons/{lessonId}/turns/{turnId}/clips` | Stores notes with optimistic semantics and manages media uploads with size/type enforcement. |

See the controllers under `src/main/java/.../controller` for the full contract, required headers (e.g., `X-User-ID`, `X-Request-Id`, `Idempotency-Key`), and request bodies.

## Data & Storage Considerations
- Uploaded media is stored under `./uploads/images` and `./uploads/audio` (configurable) and exposed via resource handlers. Ensure the application user can create these directories.
- Listening clips are saved in a dedicated `clips.base-dir` with TTL enforcement and periodic cleanup, preventing uncontrolled disk growth.
- AI-generated transcripts and audio metadata are persisted with JSON blobs for reproducibility, including coverage maps and vendor request IDs.
- Lesson deletions cascade manually: removing a material deletes its listening tasks, and deleting a folder deletes contained materials/tasks.

## Observability & Operations
- Spring Boot Actuator is enabled; combine with Micrometer Prometheus registry to scrape `/actuator/prometheus`.
- Extensive logging with structured request IDs tracks AI and TTS calls. Audio generation enforces idempotency to make retries safe.
- Global exception handlers wrap validation, entity-not-found, and upload errors into consistent JSON payloads.
- Scheduled cleanup (`@EnableScheduling`) purges expired clips each minute; adjust TTLs via `clips.ttl-seconds`.

## Testing
- Unit and slice tests cover the Lucene coverage engine plus controller flows (see `src/test/java`). Run `./mvnw test`.
- Integration tests use Testcontainers PostgreSQL where applicable.

## Repository Layout
```
src/main/java/com/mytutorplatform/lessonsservice
├── controller        # REST API entrypoints (lessons, materials, listening, AI, uploads, clips)
├── service           # Domain orchestration, AI clients, storage services, schedulers
├── model             # Entities, DTOs, Kafka payloads, enums
├── repository        # Spring Data interfaces and specifications
├── config            # Web, CORS, scheduling, static resource, and properties bindings
├── exception         # Global error handling
└── util              # Helper utilities (answer parsing/comparison, clip cleanup)
```

## Next Steps
- Wire `LessonEventService.handleLessonRescheduled` into status updates once event consumers are ready.
- Replace the default in-memory authentication with the platform’s identity provider or gateway.
- Externalize upload storage (S3, GCS, etc.) if horizontal scaling or CDN distribution is required.

This README should provide enough context to operate, extend, or integrate the Lessons Service confidently. For any domain-specific questions, inspect the relevant controller/service classes referenced above.
