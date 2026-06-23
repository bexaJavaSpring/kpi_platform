# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

KPI/task-management REST API. Spring Boot 3.3.4, Java 21, Gradle. Domain: organizations → groups → projects → tasks, with members, comments, checklists, and tags. Auth is delegated to Keycloak (OAuth2/JWT); Redis is used for caching and MinIO for file storage.

Note: `*.txt` files at the repo root (`indexing.txt`, `transactional.txt`) are study notes in Uzbek, not project docs. Many inline code comments are also in Uzbek.

## Commands

```bash
./gradlew bootRun                          # run app (local profile, port 7777)
./gradlew build                            # compile + test + package jar
./gradlew build -x test                    # build without tests (what the Dockerfile does)
./gradlew test                             # run all tests
./gradlew test --tests "uz.java.kpisystem.KpiSystemApplicationTests"   # single test class
./gradlew clean                            # clear build output

docker-compose up -d                       # start redis, redis-commander, minio, keycloak (+ its db)
```

The app depends on running Redis (host `redis:6379` per config), MinIO, Keycloak, and a Postgres `kpi_db`. Use `docker-compose.yml` for the supporting services; Postgres for the app is expected on `localhost:5432` (not in compose) in the local profile.

## Profiles & Config

Active profile defaults to `local` (`application.yml` → `spring.profiles.active: local`). Profile-specific config lives in `application-local.yml` (port 7777, `ddl-auto: update`, local DB) and `application-prod.yml` (port 7001, `ddl-auto: create`, remote DB). Config holds plaintext secrets/credentials — be careful not to leak them and avoid committing new ones.

Keycloak settings (realm `kpi-system`, client `kpi-cli`, server `localhost:8080`) live under `app.keycloak.*`. Public (no-auth) endpoints are listed under `auth.white-list` and consumed by both `SecurityConfig` and `GlobalFilter`.

## Architecture

Standard layered flow: **Controller → IService interface → Service impl → Repository (Spring Data JPA) → entity**, with **MapStruct mappers** converting between entities and DTOs.

- **Package root:** `uz.java.kpisystem`. Layers are split by package: `controller`, `service` (interface `IXxxService` + impl `XxxService`), `repository`, `entity`, `dto` (sub-packaged per domain, e.g. `dto/project`), `mapper`, `specifications`, `config`, `filter`, `exception`, `handler`.
- **Entities** extend `Auditable` (in `entity/`), which supplies `id`, audit fields (`createdAt`/`updatedAt`/`createdBy`/`updatedBy` via JPA auditing), and **soft delete**: call `entity.makeAsDeleted()` (sets `deleted=true` + `deletedAt`) instead of hard-deleting. JPA auditing is enabled via `JpaAuditingConfig`.
- **API responses:** wrap successful payloads in `ApiResponse<T>` (`data`, `success`, `message`). Note this is applied inconsistently — some endpoints return `ResponseEntity<T>` directly. Match the pattern of the controller you're editing.
- **Mappers** are MapStruct interfaces in `mapper/`; implementations are generated into `build/generated/...` at compile time (don't edit those). Convention: `toEntity`, `toResponse`, `updateFromRequest`.

### Auth & request flow

Two JWT validation paths coexist — be aware of both when touching auth:
1. **`GlobalFilter`** (`OncePerRequestFilter`, registered before `UsernamePasswordAuthenticationFilter`): extracts the Bearer token, validates via `JwtTokenService`, loads the user by Keycloak `sub` claim through `CustomUserDetailService`, and sets the `SecurityContext`. Skips white-listed paths. Also sets request locale from the `Lang` header.
2. **`SecurityConfig`** also configures `oauth2ResourceServer().jwt(...)` with a custom converter that does the same Keycloak-`sub` → `CustomUserDetails` lookup.

Sessions are **stateless**. `@EnableMethodSecurity` is on — method-level `@PreAuthorize`/`@Secured`/JSR-250 annotations are honored. `AuthService.login` calls Keycloak's token endpoint via a **Retrofit** client (`KeycloakServiceClient`); admin operations use the Keycloak admin client. Current user is available through `UserSession.getCurrentUser()`.

### Caching (Redis)

`CacheManagerService` wraps `RedisTemplate`. Cache keys are namespaced as `cachePrefix/entityId/userId` (see `CachePrefix` constants), so **cache entries are per-user**. Reads check cache first, then DB, then populate (`put` is `@Async`). Writes evict via `CacheEvictEventListener.handleCacheEvict(new XxxCacheEvictEvent(prefix))`. Eviction is role-aware: `ADMIN` clears all keys for a prefix, others only their own. Objects cached must be `Serializable` (failures surface as `RedisNotSerializableException`).

### Errors & i18n

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps custom exceptions (`CustomNotFoundException`, `GenericRuntimeException`, `JWTTokenExpiredException`, `FileStorageException`, etc.) to responses. Exception messages are i18n **message keys** (e.g. `"token.not.valid"`) resolved against `messages_{uz,en,ru}.properties` via `Translator`/`LocaleResolver`; default locale is `Uz`.

### Filtering & pagination

List endpoints take `page`/`limit`/`sortBy` params bound into a `*Filter` DTO (extending `BaseFilter`). Dynamic queries use JPA `Specification` classes in `specifications/` (`SearchSpecification`, `UserSpecification`, `GroupSpecification`).

## Conventions

- New CRUD domain: add entity (extends `Auditable`) → repository → DTOs under `dto/<domain>/` → MapStruct mapper → `IXxxService` + `XxxService` → controller. Wire caching through `CacheManagerService` + a `CachePrefix` + a cache-evict event if the domain is cached.
- Use soft delete (`makeAsDeleted()`), not repository `delete`.
- Add new public endpoints to the `auth.white-list` in the relevant `application-*.yml` if they must bypass auth.
