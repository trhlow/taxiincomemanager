# Architecture

The Taxi Income Manager is a thin three-tier system tuned for single-developer workflows and coursework-style documentation.

```
Flutter app (Dart) ──HTTPS JSON──► Spring Boot 3 REST API ──JPA/JDBC──► PostgreSQL 16
```

## Backend (`backend/`)

- **Runtime:** Java 21, Spring Boot 3.3.x, embedded Tomcat, Flyway for schema versioning.
- **Layers:**
  - **HTTP:** Controllers under `com.taxiincome.*` expose `/api/**` endpoints. Servlet filters enforce `X-Api-Key` on all `/api` calls and `Authorization: Bearer <opaque token>` for authenticated routes; `POST /api/users/init` is public (aside from the API key) and returns a one-time access token backed by hashed rows in `device_tokens`.
  - **Domain:** Entities map to Postgres via Spring Data JPA (`User`, `Order`, schedules, …).
  - **Cross-cutting:** `GlobalExceptionHandler` returns a consistent `{ "code", "message" }` body (`ApiError`). Filter responses use `FilterJsonResponses` + Jackson so the payload matches controller errors.

Orders use a split service layout: **`OrderCalculationService`** (pure money rules), **`OrderCommandService`** / **`OrderQueryService`** for writes and reads — improving test isolation and readability versus a single “god” service.

## Mobile (`mobile/`)

- Flutter 3.x, Material 3, **Riverpod** for DI and async providers, **Dio** for HTTP with interceptors injecting `X-Api-Key` and `Authorization`.
- Local preferences hold **base URL**, **user id**, **display name**, and **access token** (opaque; never send `X-User-Id` as a credential).

## Data

- Single PostgreSQL database; Flyway migrations live in `backend/src/main/resources/db/migration/`.
- Development database is typically started via root `docker-compose.yml` (host port 5433 → container 5432).

For security trade-offs and evolution, see [security-design.md](security-design.md).
