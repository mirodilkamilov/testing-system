# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

The Testing System REST API is a backend application that manages test creation, participation, and result evaluation. This project showcases backend development expertise using modern technologies and the Spring Boot framework.

## Tech stack

- **Language / build**: Java 21, Maven (via `./mvnw` wrapper)
- **Framework**: Spring Boot 3.5.x — Web (MVC), Security, Validation, DevTools
- **Persistence**: Spring Data JDBC + `JdbcTemplate` with hand-written SQL (no JPA/Hibernate)
- **Database**: PostgreSQL
- **Cache / in-memory store**: Redis (via Spring Data Redis) — JWT blacklist + test-in-progress autosave
- **Auth**: Spring Security + JJWT (`io.jsonwebtoken` 0.13.0) for HS256 JWTs
- **HATEOAS**: Spring HATEOAS 2.5.x for paginated response links
- **API docs**: springdoc-openapi 2.8.x (Swagger UI at `/api/swagger-ui.html`)
- **Testing**: Spring Boot Starter Test (JUnit 5, Mockito, Spring Test)
- **Containerization**: Docker + Docker Compose (app, PostgreSQL, Redis services)

## Commands

```bash
# Package JAR (skip tests when PostgreSQL/Redis aren't running locally)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=PageableWithFilterResolverTest

# Start full stack (app + PostgreSQL + Redis) via Docker
cp .env.example .env   # set SECRET_KEY to a 32+ char string first
docker-compose up --build

# Stop containers
docker-compose down
```

The app starts on port 8080 with context path `/api`. Swagger UI is at `http://localhost:8080/api/swagger-ui.html`.

## Architecture

### Key design choices

**No JPA/Hibernate.** All persistence goes through Spring Data JDBC and `JdbcTemplate` with hand-written SQL. There are no `@Entity` classes or JPQL queries — use raw SQL for any new queries.

**Static-method utilities with injected beans.** `AuthUtil` and similar utilities expose static methods but receive Spring beans (e.g., `StringRedisTemplate`, `PasswordEncoder`) via constructor injection into static fields. This pattern is used throughout — don't convert to instance methods.

**Custom pagination with dynamic filters.** `PageWithFilterRequest` extends Spring's `PageRequest` with a `List<FilterCriteria<?>>`. The `PageableWithFilterResolver` parses query params like `?scorePercentage>=70&isPassed=true` into typed `FilterCriteria` objects. `GenericRepositoryWithPaginationImpl` then builds parameterized SQL `WHERE`/`ORDER BY`/`LIMIT`/`OFFSET` clauses via `DataUtil.appendWhereClause` and `appendOrderByClause`.

**Two-filter error handling.** Exceptions thrown inside the Spring Security filter chain (before controllers) are caught by `ExceptionHandlerFilter`. Exceptions from controllers/services are handled by `GlobalControllerExceptionHandler`. Both produce the same `GenericErrorResponse` shape.

### Layer structure

| Package | Purpose |
|---|---|
| `configs/` | Spring Security, JWT filter, CORS, OpenAPI, `PageableWithFilterResolver` |
| `controllers/` | `AuthController`, `UserManagementController` (admin), `TestTakerController`, `TestEventController` |
| `services/` | `AuthService`, `UserService`, `TestEventService` |
| `repositories/` | `JdbcTemplate`-based repos; `CustomUserRepository`/`CustomTestEventRepository` for complex queries; `GenericRepositoryWithPagination` for paginated list endpoints |
| `models/` | Plain Java classes (no JPA annotations); enums in `models/enums/` |
| `dtos/` | Request/response DTOs with Bean Validation annotations |
| `utils/` | `AuthUtil` (JWT + token blacklist), `AppUtil`, `DataUtil` (SQL builders), `ValidationUtil` |
| `exceptions/` | Custom exception classes + handlers |
| `validations/` | Custom constraint annotations (`@UniqueEmail`, `@ValidTestEventPageRequest`, etc.) |

### Auth flow

1. JWT token (1-day expiry, HS256) generated on login via `AuthUtil.generateToken`.
2. `JwtAuthenticationFilter` validates the token and populates `SecurityContextHolder` on every request.
3. On logout, the token is stored in Redis with a 1-day TTL (`AuthUtil.blacklistToken`). Subsequent requests with that token throw `InvalidTokenException`.
4. Redis is also used for autosaving test-in-progress state.

### Database

Schema lives in `docker/sql/01_create_tables.sql` (tables + `test_events_view`) and `02_insert_data.sql` (seed data). The `test_events_view` joins `test_events`, `users`, `roles`, and `tests` — most test-event queries read from this view.

### URL routing

- `/auth/**` — public (login, refresh)
- `/test-taker/register` — public
- `/admin/**` — `ADMIN` role only
- `/test-taker/**` — `ADMIN` or `TEST_TAKER`
- Everything else — any authenticated user

### Environment variables

Required in `.env` (see `.env.example`):
- `SECRET_KEY` — JWT signing key, must be ≥ 32 characters
- `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`

In `docker-compose.yml` the app container overrides `SPRING_DATA_REDIS_HOST=redis` (the Docker service name). For local development outside Docker, point Redis to `localhost`.

## Implementation status

### Done

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login`, `POST /auth/logout` |
| Test Taker | `POST /test-taker/register`, `GET /test-taker/profile` |
| Admin – Users | `GET /admin/profile`, `GET /admin/users`, `GET /admin/users/{id}`, `POST /admin/users` |
| Admin – Test Events | `GET /admin/test-events`, `GET /admin/test-events/{id}` |

Infrastructure fully in place: JWT auth, Redis token blacklist, Spring Security with permission-based access (`@PreAuthorize`), custom pagination/filter framework, OpenAPI docs. DB schema for `questions`, `options`, and `question_types` also exists but has no endpoints yet.

### Not yet built

**Admin – User management**
- `PUT /admin/users/{id}` — update user details
- `PATCH /admin/users/{id}/status` (or similar) — deactivate/reactivate account

**Admin – Test management** (no `TestController` or service exists yet)
- `POST /admin/tests` — create test with title, description, duration, no_of_questions, passing_percentage, should_shuffle, should_randomly_pick
- `GET /admin/tests`, `GET /admin/tests/{id}`
- `PUT /admin/tests/{id}`, `DELETE /admin/tests/{id}` (soft-delete via `deleted_at`)
- `POST /admin/tests/{id}/questions` — add questions (MCQ, Checkbox, True/False, Short Answer, Essay, Matching, Fill-in-the-Blank); points 0–5 integers
- `PUT`/`DELETE` on individual questions and options

**Admin – Test event management**
- `POST /admin/test-events` — assign test to a test taker with date/time
- `PUT /admin/test-events/{id}` — update event
- `GET /admin/test-events/{id}/test-attempt` — view submitted answers (endpoint stub exists, marked TODO)

**Test Taker – Test participation** (no endpoints exist yet)
- `GET /test-taker/test-events` — list assigned test events and results
- `POST /test-taker/test-events/{id}/start` — start test, sets status to `IN_PROGRESS`, begins timer
- `PUT /test-taker/test-events/{id}/autosave` — save progress to Redis mid-test
- `POST /test-taker/test-events/{id}/submit` — submit answers, trigger auto-evaluation, set score/pass-fail

**Evaluation logic** — automatic grading for objective question types (MCQ, Checkbox, True/False); manual review path for Short Answer and Essay questions

### Seed data defaults
- Admin: `info@mirodil.dev` / `12345`
- 10 test-taker users, 7 tests, 6 test events pre-seeded
