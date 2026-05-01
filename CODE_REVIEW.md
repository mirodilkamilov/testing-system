# Code Review — Testing System REST API

**Reviewer perspective:** Senior Java Backend Developer
**Scope:** Static review of `src/`, `pom.xml`, `docker-*`, `docker/sql/*`, plus runtime probing of `http://localhost:8080/api`.
**Branch reviewed:** `experimenting-claude-code` (10 commits past `main`).

---

## TL;DR

This is genuinely impressive work for someone at the junior level. You picked an ambitious stack (Spring Security + JWT + Redis + PostgreSQL + dynamic filtering + HATEOAS + OpenAPI), wired it together cleanly, and the running app behaves correctly under poking — auth flow works, role gates hold, blacklist works after logout, sort-injection is rejected, validation is wired, and CORS preflight is sane. You're already past "can build a CRUD app" and into "can architect a multi-component system."

That said, several decisions are going to bite you the moment this leaves your laptop, and a few are anti-patterns I'd flag in any production review regardless of seniority. The biggest one is the **static-state utility classes that hold injected Spring beans in static fields** — fix that pattern first, and a lot of your testing pain disappears.

Below: strong parts, weak parts, and a concrete order to attack them in.

---

## 1. Strong parts

### 1.1 Architecture & layering

- **Clear package boundaries.** `controllers/`, `services/`, `repositories/`, `dtos/`, `models/`, `configs/`, `exceptions/`, `validations/`, `utils/` — every file you'd expect is where you'd expect it. A new dev would orient in 10 minutes.
- **Constructor injection everywhere.** No `@Autowired` field injection. This alone puts you ahead of a lot of code I see in the wild.
- **Records for DTOs.** `UserResponseDTO`, `UserCreateRequestDTO`, `TestEventResponseDTO`, etc. — modern, immutable, less boilerplate.
- **Two-layer error handling** is the right call: `ExceptionHandlerFilter` for things that explode inside the security filter chain, `@ControllerAdvice` for everything past it. Both produce the same JSON shape, which I verified at runtime.
- **HATEOAS on paged responses** (`self`, `first`, `last`, `next`, `previous`) and on individual resources (`path` field) — nice touch most juniors skip.

### 1.2 Security fundamentals

- **JWT with HMAC-SHA256, stateless session, BCrypt password hashing** — textbook setup, correctly applied.
- **Token blacklist via Redis with a TTL matching the token expiry** — clever; most juniors don't even know "logout" is a hard problem with stateless JWTs.
- **Permission-based authorization on top of role-based**, gated through `@PreAuthorize("hasAuthority('MANAGE_ALL_USERS')")`. The `roles → role_permissions → permissions` join table is properly normalized.
- **CORS is explicitly allow-listed**, not `*`. You'd be shocked how often I see `setAllowedOrigins(List.of("*"))`.
- **Sort-injection defense is real.** I tested `?sort=fname;DROP TABLE users;--` against `/admin/users` and got a clean 400 with the allow-list dumped to the response. `ValidationUtil.forceValidPageable` does the right thing.

### 1.3 Custom pagination/filter framework

`PageableWithFilterResolver` + `PageWithFilterRequest` + `FilterCriteria` + `DataUtil.appendWhereClause` is the most ambitious piece of code in the project. It:

- Parses operators directly out of query keys (`?score>=70`, `?score>70`, `?score=70`, `?status=active,inactive`).
- Validates allowed sort/filter attributes per entity via marker annotations (`@ValidUserPageRequest`, `@ValidTestEventPageRequest`).
- Delegates type coercion (Integer/Long/Boolean/Instant/String) with clear errors.
- Builds parameterized SQL (`?` placeholders, never string concatenation of values).
- Has a respectable unit test suite (`PageableWithFilterResolverTest`) that even covers edge cases like spaces, malformed operators, multi-value with mixed operators.

The framework itself is over-engineered (see §2.3), but the *intent* and *injection-safety* are solid.

### 1.4 Data layer choices

- Choosing **Spring Data JDBC + raw SQL over Hibernate/JPA** is a defensible, mature choice for a project of this size. You know exactly what SQL fires; no LazyInitializationException nightmares.
- **`test_events_view`** to centralize the join across `test_events`, `users`, `roles`, `tests` is the right move — keeps the read query in the DB instead of in app code.
- **Indexes**, **`UNIQUE`** on `users.email`, **`ON DELETE CASCADE`** on options/questions, **`ON DELETE SET NULL`** on `test_events.test_id` — all thoughtful.
- `@Column("event_datetime")` to bridge naming mismatches. Good awareness of Spring Data JDBC conventions.

### 1.5 Tooling

- **Dockerized stack with PostgreSQL + Redis + app** — `docker-compose up --build` and you're running.
- **OpenAPI/Swagger UI** wired with a proper `@OpenAPIDefinition` — including server URLs, security scheme, contact, license. Looks professional.
- **Dependabot** weekly schedule. Fewer juniors set this up than you'd think.
- **`.env.example`** committed (without secrets), and the README walks through setup.

---

## 2. Weak parts

I'll group these by severity. The "🔴 Critical" ones I would block a PR on; "🟠 Major" I'd request changes on; "🟡 Minor" I'd flag as nits.

### 🔴 Critical

#### 2.1 `AuthUtil` is a static-state singleton holding injected Spring beans

```java
@Component
public class AuthUtil {
    private static final String secretKeyPlain = System.getenv("SECRET_KEY");
    private static final SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    private static PasswordEncoder passwordEncoder;
    private static StringRedisTemplate redisTemplate;

    public AuthUtil(StringRedisTemplate redisTemplate, PasswordEncoder passwordEncoder) {
        AuthUtil.redisTemplate = redisTemplate;
        AuthUtil.passwordEncoder = passwordEncoder;
    }
    ...
}
```

This pattern is bad in three independent ways:

1. **`secretKeyPlain` and `key` are `static final`, initialized at class-load time from `System.getenv`.** That bypasses Spring's environment abstraction entirely — your app will not respect `application.properties`, profile-specific configs, or any external secrets manager (Vault, AWS Secrets Manager, etc.). And if `SECRET_KEY` env var isn't set at JVM start, classloading throws `NullPointerException` on `secretKeyPlain.getBytes()`. There is no graceful failure.

2. **The constructor mutates static fields.** This is the classic "static singleton with hidden DI" anti-pattern. Side effects:
   - You cannot mock `AuthUtil` in unit tests with Mockito (it's all static).
   - In integration tests, the *first* `AuthUtil` instance "wins" — if Spring rebuilds the context (e.g., `@DirtiesContext`), the old static reference may be stale.
   - Two parallel test contexts → race condition on the static field.
   - You cannot have two configurations (e.g., one `PasswordEncoder` for tests vs prod) coexist.

3. **Single-responsibility violation.** `AuthUtil` does JWT signing, JWT parsing, token blacklisting, password hashing, password matching, SecurityContext mutation, *and* HATEOAS link building. That's at least 4 services pretending to be one class.

**Fix:**
- Replace with proper `@Service` classes: `JwtTokenService`, `TokenBlacklistService` (already a thin wrapper around Redis — could just be inline), and use the existing `PasswordEncoder` bean directly where needed.
- Inject `@Value("${jwt.secret}")` so Spring's environment abstraction owns secrets resolution.
- Remove all static methods. Use instance methods + DI.

This is the single biggest unlock — once `AuthUtil` is a real bean, you can write meaningful unit tests for `AuthService`, `JwtAuthenticationFilter`, etc.

#### 2.2 No rate limiting on `/auth/login`

I just brute-forced `info@mirodil.dev` against your local instance for fun (only with the right password — but I could've kept guessing). Nothing in the code limits attempt rate. Combined with a default password of `12345` and a `@Size(min = 5)` on passwords, this is a credential-stuffing playground.

**Fix:** add a rate limiter on `/auth/login` (Bucket4j + Redis, or Resilience4j RateLimiter). Same for `/test-taker/register` to stop email enumeration / spam.

#### 2.3 Password complexity is `@Size(min = 5, max = 50)` — that's it

`12345` is a valid password in your system *today*. There's no minimum entropy, no symbol/case requirements, no breach-list check.

**Fix:** raise minimum to 8 (NIST SP 800-63B agrees), add a regex constraint, optionally check against the [HIBP k-anonymity API](https://haveibeenpwned.com/API/v3#PwnedPasswords) on registration. Since this is a school/work testing system, even `@Pattern` enforcing one digit + one letter would be a step up.

#### 2.4 `JwtAuthenticationFilter` does the DB lookup *before* checking expiry

```java
String username = AuthUtil.extractUsername(token);   // parses claims (JJWT also checks expiry here, but...)
AuthUtil.checkTokenBlacklisted(token);                // Redis hit
User user = userService.loadUserByUsername(username); // DB hit
if (AuthUtil.isTokenExpired(token)) {
    throw new InvalidTokenException("Token expired");
}
```

JJWT 0.13's `parseSignedClaims` already throws `ExpiredJwtException` on expired tokens (caught in `ExceptionHandlerFilter`). So the explicit `isTokenExpired` check is redundant — but worse, **the DB lookup happens for expired tokens**. An attacker holding old expired tokens could replay them at high volume to amplify load on your DB and Redis. Cheap DoS.

**Fix:** trust JJWT's parser to validate signature + expiry, do the blacklist check, and only *then* hit the DB. Or, even better, encode `userId` as a JWT claim and skip the DB lookup entirely if you're willing to reissue tokens on permission changes (classic JWT trade-off).

Also, the filter has confusing dual `filterChain.doFilter(request, response)` calls and a TODO that says "later make it stateful" — directly contradicting the JWT design. Pick a strategy and stick to it.

#### 2.5 Logout endpoint behavior is fragile

`/auth/logout` is in `WHITE_LIST_URL` (`permitAll`). The flow:
1. Anyone can hit `POST /auth/logout`.
2. `AuthUtil.extractTokenFromRequest` is called; if no Bearer header → `InvalidTokenException("Authorization header is missing or invalid", BAD_REQUEST)`.
3. If a Bearer header is present (any value), it's blacklisted in Redis.

You're using the **token itself as the Redis key**. A garbage Bearer header (`Bearer aaa`) gets stored in Redis. So an attacker can stuff your Redis with junk keys for free, no auth needed.

**Fix:**
- Move `/auth/logout` *out* of the whitelist so the JWT filter validates the token first.
- Use the token's `jti` claim (or `SHA-256(token)`) as the Redis key, not the raw 200+ char token.

### 🟠 Major

#### 2.6 `UserResponseDTO` reaches into the SecurityContext

```java
private static URI resolvePath(User user) throws RuntimeException {
    UserRole currentRole = UserRole.TEST_TAKER;
    if (AuthUtil.isUserAuthenticated()) {
        currentRole = AuthUtil.getAuthenticatedUserRole();
    }
    switch (currentRole) {
        case ADMIN -> ...
        case TEST_TAKER -> ...
    }
}
```

A DTO is supposed to be a pure data carrier. This one:
- Reads from the SecurityContext (a thread-local).
- Builds HATEOAS links via `WebMvcLinkBuilder`, which requires `RequestContextHolder` to be populated.
- Therefore breaks the moment you try to construct a `UserResponseDTO` outside an HTTP request thread (async tasks, scheduled jobs, batch processing, tests).

**Fix:** introduce a `UserResponseAssembler`/`UserMapper` that takes `User` + the current role/request and produces the DTO. Keep DTOs dumb.

#### 2.7 Reinventing HATEOAS pagination

`PagedResponse<T>` with `generateLinks` doing regex replacements on URLs is brittle and reinvents `org.springframework.data.web.PagedResourcesAssembler` / `PagedModel`. I'd port it.

The regex-on-URL approach also has subtle bugs around encoded chars; e.g. `?scorePercentage%3E=70&page=0` → the link generation appends `&page=N` but doesn't normalize the existing query, and you end up with `&` after `?` etc. — your existing handling kind of works but reads like it was hand-tuned around bugs.

#### 2.8 The filter framework's UX is hostile

The CLAUDE.md and OpenAPI doc both show `?scorePercentage>=70` as the example. I tried it: **Tomcat rejects it with a raw HTML 400**, because `>` is not allowed unencoded in URI query syntax (RFC 3986). You have to do `%3E`. None of the docs mention this; new clients will trip over it.

Two paths:
- Switch to a more standard convention: `?score=gte:70&score=lte:90` or the RSQL/FIQL syntax (`?filter=scorePercentage>=70;scorePercentage<=90` with `;` URL-safe), or Spring Data REST-style `?score.gte=70`.
- Or document the encoding requirement loudly and provide a Postman collection with pre-encoded examples (which you already do — the README links to it).

Either way, "bare `>` in a URL" is going to keep biting you.

Also, the parser logic in `extractFilterParameters` (regex of `([<>])(.*)` against the param *name*, conditional `=` re-attach based on `value.isBlank()`) is genuinely hard to follow on a second read. The unit tests prove it works, but the cognitive load is high.

#### 2.9 `DataUtil.appendWhereClause` always uses `LIKE %value%` for strings

```java
} else if (value instanceof String) {
    queryParams.add("%" + value + "%");
    conditions.add(attribute + " LIKE ?");
}
```

That means filtering by `email=info@mirodil.dev` does a substring search, not exact match. Side effects:
- Cannot use the unique index on `users.email` for a lookup that conceptually *is* an exact-match lookup.
- Performance scales linearly with row count (sequential scan) for what should be O(log N) with the index.
- No way to do exact-match string filter in the current API. Caller has no opt-in.

**Fix:** distinguish "starts with" / "contains" / "exact" via operator: `email=foo@bar.com` exact, `email~=foo` substring, etc. Or accept a `*` wildcard convention.

#### 2.10 Inconsistent response envelopes

Looking across the controllers:
- `AuthController.login` → `WrapResponseWithContentKey<AuthResponseDTO>`
- `AuthController.logout` → `Map<String, String>` (raw `{"message": "..."}`)
- `UserManagementController.getProfile` → `WrapResponseWithContentKey<UserResponseDTO>`
- `UserManagementController.getAllUsers` → `PagedResponse<UserResponseDTO>` (no wrapper)
- `UserManagementController.createUser` → `WrapResponseWithContentKey<UserResponseDTO>` inside `ResponseEntity.created(...)`
- Errors → `Map<String, Object>` from `GenericErrorResponse`

Three or four different shapes for "success" responses. Pick one (most APIs use `{ data: ..., meta: ..., errors: ... }`) and apply it everywhere. Frontend devs will thank you.

#### 2.11 No transactions

Search the project for `@Transactional` — it's not there. Right now most operations are single-row inserts so it doesn't matter, but the moment you add the test-management endpoints (`POST /admin/tests/{id}/questions` writes to `questions` AND `options`, `POST /test-taker/test-events/{id}/submit` updates `test_events` AND has to compute `score_points`), you need transaction boundaries.

**Fix:** mark service methods with `@Transactional` (read-only by default at class level, override on writes) the moment a method does more than one DB call.

#### 2.12 `User.getAuthorities()` will NPE on the shallow constructor

```java
public User(Long id, String email, String fname, String lname) { ... }   // doesn't set permissionNames

public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toString().toUpperCase()));
    permissionNames.forEach(...);   // NPE if permissionNames is null
    return grantedAuthorities;
}
```

`extractTestTakerFromResultSet` builds a User via the shallow constructor — `permissionNames` and `role` are null. If anything downstream calls `.getAuthorities()` on that nested object, it dies.

**Fix:** initialize collections in the field declaration (`private Set<PermissionType> permissionNames = Set.of();`) or null-guard in `getAuthorities`.

#### 2.13 `User` model = JDBC entity + `UserDetails` + DTO source

Mixing the persistence model with `UserDetails` is a common Spring pattern and not strictly wrong, but at scale you'll want a `UserPrincipal implements UserDetails` wrapper — keeps the domain model free of Spring Security types and makes the security boundary explicit.

#### 2.14 Static utility classes in general

Beyond `AuthUtil`: `DataUtil`, `AppUtil`, `ValidationUtil` are all "static methods that act on parameters." `DataUtil` is a kitchen sink — SQL clause builders, ResultSet extractors for User/Role/Permission/Test/TestEvent, an ObjectMapper, snake-case conversion. Six responsibilities in 270 lines.

**Fix:** split into `SqlClauseBuilder` (the WHERE/ORDER BY logic), per-entity `RowMapper`s registered as beans (you already have `userRowMapper` and `testEventRowMapper` — keep going), and a `Naming` util for case conversion if needed.

### 🟡 Minor / Nits

#### 2.15 Naming clashes

- `dev.mirodil.testing_system.exceptions.ResponseStatusException` shadows `org.springframework.web.server.ResponseStatusException`. Rename to `BaseHttpException` or extend Spring's class.
- `dev.mirodil.testing_system.models.Test` clashes with `org.junit.jupiter.api.Test`. Rename to `Quiz` / `Exam` / `Assessment` — you'll thank yourself when you write tests for the Test management controller.

#### 2.16 Dockerfile is not self-contained

```dockerfile
FROM eclipse-temurin:21-jdk
COPY target/testing-system-0.0.1-SNAPSHOT.jar /app/target/testing-system.jar
```

You have to remember to run `./mvnw clean package` *before* `docker-compose up --build`. CI doesn't know that. Use a multi-stage build:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/testing-system-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Also: production image should use `21-jre`, not `21-jdk` — saves ~200 MB.

#### 2.17 No `depends_on: condition: service_healthy`

```yaml
depends_on:
  - db
  - redis
```

Compose only waits for the containers to *start*, not to be *ready to accept connections*. Cold-start race: app comes up before Postgres opens its socket. Fix:

```yaml
db:
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER"]
    interval: 5s
    retries: 5
app:
  depends_on:
    db:
      condition: service_healthy
    redis:
      condition: service_started
```

#### 2.18 Redis blacklist is non-persistent

The Redis container has no `appendonly yes` or persistence config. If Redis restarts, all blacklisted JWTs become valid again until they expire naturally. Mention this in your threat model, or persist Redis (`command: ["redis-server", "--appendonly", "yes"]` + a volume).

#### 2.19 `logging.level.org.springframework.web=debug` in `application.properties`

DEBUG-level web logging dumps request bodies and headers to stdout. Do NOT ship this to prod. Use Spring profiles:

- `application.properties` — sane defaults
- `application-dev.properties` — your DEBUG noise
- `application-prod.properties` — INFO + structured JSON logger

#### 2.20 `pom.xml.versionsBackup` and `target/` artifacts not in `.gitignore`

`pom.xml.versionsBackup` is showing up as untracked in `git status`. Add to `.gitignore`:
```
*.versionsBackup
.env
```

#### 2.21 `Test.deleted_at` snake_case field

```java
private Instant deleted_at;
```

Inconsistent with the rest of the codebase. Rename to `deletedAt` and add `@Column("deleted_at")` if Spring Data JDBC's name resolver complains.

#### 2.22 `UniqueEmailValidator` uses exception flow as control flow

```java
try {
    userService.loadUserByUsername(email);
} catch (UsernameNotFoundException e) {
    return true;
}
return false;
```

Add a dedicated `userRepository.existsByEmail(String)` (Spring Data JDBC supports it natively) and call that. Faster and clearer.

Also, this validator is TOCTOU-vulnerable (between validation and INSERT). The DB's `UNIQUE` constraint catches the race, but you should handle `DataIntegrityViolationException` in the service and return a clean 409 Conflict — currently it'll bubble as a 500.

#### 2.23 No actuator

Add `spring-boot-starter-actuator` and expose at least `/actuator/health` and `/actuator/info`. Standard for any prod Spring Boot service. Liveness/readiness probes need it.

#### 2.24 Tests are minimal

Two test files: `PageableWithFilterResolverTest` (excellent) and `TestingSystemApplicationTests.contextLoads()` (the autogenerated Spring Boot one).

You are missing:
- Service-layer unit tests (mock the repos, test branching).
- Controller tests with `@WebMvcTest` + `MockMvc`.
- `@DataJdbcTest` slices for repositories.
- Integration tests with **Testcontainers** for PostgreSQL + Redis. *This is the gold standard for projects like this.* Spin up real Postgres in CI, run real SQL, real Redis, real JWT round-trips.

The fact that you DID write tests for the trickiest piece (the filter resolver) tells me you can write tests when you decide to. Just keep going.

#### 2.25 No CI

You have `.github/dependabot.yml` but no `.github/workflows/*.yml`. Add a basic CI:

```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: maven }
      - run: ./mvnw -B verify
```

#### 2.26 `Test` model — `shouldShuffle()` getter naming

```java
public Boolean shouldShuffle() { return shouldShuffle; }
public Boolean shouldRandomlyPick() { return shouldRandomlyPick; }
```

Jackson uses JavaBean conventions (`get*` / `is*` for booleans). `shouldShuffle()` will not be picked up by default — it works in your code only because you reference it manually in `TestResponseDTO`. If anyone serializes a `Test` directly via Jackson, those fields will be invisible.

**Fix:** rename to `isShouldShuffle()` (ugly but JavaBean-compliant) or `getShouldShuffle()`, or annotate with `@JsonProperty("shouldShuffle")`.

#### 2.27 No request correlation / structured logging

Once this is in prod and a user reports "my login failed at 3pm", you want to grep one request ID across logs. Add Micrometer Tracing (`spring-boot-starter-actuator` + `micrometer-tracing-bridge-otel`) and a JSON logger (`logstash-logback-encoder`).

#### 2.28 Default admin password in seed data

`info@mirodil.dev` / `12345` is fine for local dev. Just be **extremely** sure the deployed `https://testing.mirodil.dev` doesn't have it. Loudly document in README that the seed admin must be rotated post-deploy.

---

## 3. Suggested next steps — *inside* this project

Order matters. Don't rip everything up at once.

### Phase 1 — Foundation cleanups (1-2 days)

1. **Kill `AuthUtil` static state** (§2.1). Convert to `JwtTokenService` (instance methods, `@Value("${jwt.secret}")` injection), drop the static helpers, fix the call sites. This will mostly be mechanical.
2. **Rename `models.Test` → `Quiz` / `Exam`** (§2.15) and `exceptions.ResponseStatusException` → `BaseHttpException`. Do this *before* you write more tests.
3. **Add Testcontainers for Postgres + Redis** and write *one* end-to-end integration test for the login flow as proof of concept. Once that's green, the next 20 tests are basically free.
4. **Add CI** (§2.25). Run `./mvnw verify` on every push. Block PRs on red.
5. **Multi-stage Dockerfile** + healthcheck-aware `docker-compose` (§2.16, §2.17).

### Phase 2 — Security hardening (2-3 days)

6. **Rate-limiting on `/auth/login` and `/test-taker/register`** (§2.2). Bucket4j + Redis is ~50 lines.
7. **Move `/auth/logout` out of the whitelist, hash the token before storing in Redis** (§2.5).
8. **Fix the JWT filter ordering** so DB lookup only happens for non-expired, non-blacklisted tokens (§2.4).
9. **Stronger password rules + `existsByEmail`** instead of try/catch (§2.3, §2.22).
10. **Profile-based logging configuration** (§2.19). DEBUG only in `dev` profile.

### Phase 3 — Build out the missing features (the meat of the project)

The CLAUDE.md is honest about what's missing. Suggested order:

11. **`POST /admin/tests` + CRUD for tests** — straightforward CRUD, builds on patterns you already have. Use `@Transactional` from day one.
12. **Question/Option management** under tests — you'll hit your first non-trivial transactional boundary (creating a test with N questions and M options atomically).
13. **`POST /admin/test-events`** — assigning a test to a taker with date/time. Add validation: future-dated, taker is `TEST_TAKER` role, test is not soft-deleted.
14. **Test-taker participation flow** — `start`, `autosave` (Redis!), `submit`. This is where the design really pays off; the autosave Redis store is a great call.
15. **Auto-evaluation engine** — start with MCQ + Checkbox + True/False (objective). Defer Short Answer/Essay to a manual review queue.

### Phase 4 — Production-readiness

16. **Actuator + health checks + structured JSON logs + tracing.**
17. **Caching** for `loadUserByUsername` with a short TTL (e.g., 30s) to reduce DB load on every authenticated request. Cache invalidation on user update is the only tricky bit.
18. **Refactor the filter framework** — the param-name regex parsing is the riskiest code in the repo. Either move to a documented standard (RSQL) or simplify your own dialect.
19. **PagedResourcesAssembler** to replace `PagedResponse` (§2.7).
20. **OpenAPI examples** — fill in `@Operation` examples and Swagger UI's "Try it out" UX gets dramatically better.

---

## 4. Suggested next steps — *outside* this project (career)

You're at the level where the right next moves are about *breadth* and *production exposure*, not just more Spring Boot.

### 4.1 Things to learn next, in priority order

1. **Testcontainers + Spring Boot integration tests, deeply.** This single tool changes how you ship code. Master it. Once you can spin up a real Postgres + Redis + Kafka + S3 (LocalStack) for a single test, you stop being scared of integration tests.
2. **One messaging system end-to-end.** Pick **Kafka** (or RabbitMQ / SQS). Build a project where one service produces events, another consumes them. Idempotency, dead-letter queues, retries, exactly-once vs at-least-once. This is table-stakes for any serious backend role.
3. **Observability trifecta**: metrics (Micrometer + Prometheus), logs (structured JSON + ELK or Loki), traces (OpenTelemetry + Jaeger/Tempo). Build *one* service where you can correlate a slow API call → a span → the metric → the log line. Once you've done it once, you understand prod debugging.
4. **AWS or GCP fundamentals.** S3, RDS, ECS/EKS, CloudWatch, IAM, VPC. You don't need to memorize every service — you need to know the shape of "deploy a Spring Boot app to a managed runtime, point it at a managed Postgres, ship logs and metrics." This unlocks "Cloud Deployment" being more than a bullet on your README.
5. **Database deeper than the ORM.** Read *Designing Data-Intensive Applications* by Kleppmann (everyone says this, because it's true). Then practice: `EXPLAIN ANALYZE` your queries, understand B-tree indexes, learn what a sequential scan is and when it's fine, learn about transaction isolation levels (you currently have *none* set — defaults are READ COMMITTED in Postgres). Try to break your own app under concurrent writes.
6. **One non-Java language.** Pick **Go** or **Python** (or both). Not to abandon Java — to broaden how you think. Go will teach you how to think about concurrency without `synchronized`. Python will teach you fast iteration. Either makes you a better Java dev.

### 4.2 Habits that compound

- **Read other people's Spring Boot code on GitHub.** Big public repos: [spring-petclinic](https://github.com/spring-projects/spring-petclinic) (canonical), [piggymetrics](https://github.com/sqshq/piggymetrics) (microservices), real OSS like [keycloak](https://github.com/keycloak/keycloak) or [signal-server](https://github.com/signalapp/Signal-Server). You'll absorb idioms by osmosis.
- **PR reviews — yours and others'.** Even on your own projects, make every change a PR (against `main`). Force yourself to write the description, justify the diff, then read it back as if you were the reviewer.
- **Write one technical blog post per project.** Doesn't have to be polished. "What I learned building X." This solidifies what you know and shows up on your resume.
- **Join an OSS project.** Find a Spring-related repo with a "good first issue" label. The first PR is the hardest; after that it gets easy. Real-world code review from senior maintainers is worth more than any course.
- **Learn `git` past `add`/`commit`/`push`.** `rebase -i`, `cherry-pick`, `bisect`, `reflog`, conflict resolution. These come up daily at any non-trivial engineering job.

### 4.3 Things I would *not* spend time on right now

- **Microservices.** Don't split this app into 8 services for the sake of "experience." Monoliths are fine; a well-structured Spring Boot monolith is an *asset*, not a liability. You'll learn way more about microservices by joining a team that has them than by inventing them yourself.
- **Reactive Spring (WebFlux).** It's a niche. The performance argument almost never applies to apps your size. Learn classic Spring MVC + thread-pool tuning first; come back to reactive if you ever need it.
- **Premature Kubernetes.** Docker Compose is great. Knowing K8s helps for jobs, but you can skim the surface (one minikube tutorial) and that'll be enough until a job requires more.

### 4.4 Job-search-shaped advice

- This project, *cleaned up per Phase 1-2 above*, is portfolio-worthy. Pin it on your GitHub. Make sure the README's first paragraph answers "what does this do" in one sentence.
- Add a **"Design decisions"** section to the README explaining why you chose JDBC over JPA, why JWT + Redis blacklist, etc. Recruiters skim; engineers read this section closely.
- The `https://testing.mirodil.dev` deployment alone puts you ahead of 80% of junior portfolios — a deployed thing matters.
- When interviewing, the questions you'll get on this project are: *"What would you do differently if you started today?"* Have an answer. The fact that you can articulate the `AuthUtil` static-state issue in your own code is the single best signal of seniority.

---

## 5. Closing note

You're not far from the next level. The structural sense is already there — packaging, layering, security awareness, willingness to write tests for the hard parts. What separates you from a mid-level dev today is mostly **production discipline** (testability, observability, transaction boundaries, fail-safe defaults) and **conservative engineering instincts** (don't reinvent `PagedModel`, don't statify a class that shouldn't be static, prefer boring framework features over clever custom ones).

Fix the static-state utility classes first. Add Testcontainers second. Everything else gets easier from there.

Keep going.