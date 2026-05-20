# 🏥 Pharmacie en Ligne — Admin Workflow

> **Module:** INF4121 – Développement Back-End Avancé · Session Printemps 2026  
> **Team:** Ayman Allouch · Sallahedin Hamzi · Sami Atlagh · Walid Abaaqil  
> **Scope:** Administrator vertical slice — from DB schema to REST API, security, patterns & tests

---

## Table of Contents

1. [Scope & Responsibilities](#1-scope--responsibilities)
2. [Domain Model & Database Schema](#2-domain-model--database-schema)
3. [Package Structure](#3-package-structure)
4. [Design Patterns](#4-design-patterns)
5. [REST API Specification](#5-rest-api-specification)
6. [Security Configuration](#6-security-configuration)
7. [Step-by-Step Implementation Guide](#7-step-by-step-implementation-guide)
8. [Test Strategy](#8-test-strategy)
9. [Team Task Split](#9-team-task-split)
10. [Maven Dependencies](#10-maven-dependencies)
11. [Definition of Done](#11-definition-of-done)

---

## 1. Scope & Responsibilities

The administrator supervises the entire platform without performing patient or pharmacist business actions.

**Admin capabilities:**
- View and manage all user accounts (patients and pharmacists)
- View, activate, or deactivate pharmacy accounts
- Access a supervision dashboard with real-time KPIs
- Dedicated JWT role: `ROLE_ADMIN`

---

## 2. Domain Model & Database Schema

### 2.1 Entities Owned by Admin Workflow

| Entity | Table | Notes |
|--------|-------|-------|
| `AdminUser` | `admin_users` | Extends base user; stores role override |
| `Pharmacy` | `pharmacies` | Shared with pharmacist team; admin toggles `active` flag |
| `User` (base) | `users` | Shared; admin reads all rows |
| `Prescription` | `prescriptions` | Read-only for admin (stats) |
| `Order` | `orders` | Read-only for admin (stats) |
| `AuditLog` | `audit_logs` | Written by admin actions via Observer pattern |

### 2.2 Flyway Migration — `V3__admin_schema.sql`

```sql
-- Add active flag to pharmacies (shared table)
ALTER TABLE pharmacies
  ADD COLUMN IF NOT EXISTS active TINYINT(1) NOT NULL DEFAULT 1;

-- Admin user association table
CREATE TABLE admin_users (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT NOT NULL UNIQUE,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Audit trail for every admin action
CREATE TABLE audit_logs (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  admin_id    BIGINT NOT NULL,
  action      VARCHAR(100) NOT NULL,
  target_type VARCHAR(60),
  target_id   BIGINT,
  detail      TEXT,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (admin_id) REFERENCES users(id)
);
```

---

## 3. Package Structure

```
src/main/java/com/pharmacy/
└── admin/
    ├── controller/
    │   ├── AdminUserController.java
    │   ├── AdminPharmacyController.java
    │   └── AdminDashboardController.java
    ├── service/
    │   ├── AdminUserService.java
    │   ├── AdminPharmacyServiceI.java        ← interface
    │   ├── AdminPharmacyService.java          ← impl
    │   ├── LoggingAdminPharmacyService.java   ← decorator (@Primary)
    │   └── AdminDashboardService.java         ← singleton
    ├── repository/
    │   ├── AdminUserRepository.java
    │   └── AuditLogRepository.java
    ├── dto/
    │   ├── UserSummaryDTO.java
    │   ├── PharmacySummaryDTO.java
    │   ├── DashboardStatsDTO.java
    │   └── PharmacyStatusUpdateDTO.java
    ├── entity/
    │   ├── AdminUser.java
    │   └── AuditLog.java
    ├── observer/
    │   ├── AdminActionEvent.java
    │   └── AuditLogListener.java
    ├── factory/
    │   ├── Notification.java
    │   ├── EmailNotification.java
    │   ├── SmsNotification.java
    │   └── NotificationFactory.java
    ├── security/
    │   └── AdminSecurityConfig.java
    └── exception/
        └── GlobalExceptionHandler.java

src/test/java/com/pharmacy/admin/
    ├── AdminUserServiceTest.java
    ├── AdminPharmacyServiceTest.java
    ├── AdminDashboardServiceTest.java
    ├── AuditLogListenerTest.java
    ├── AdminPharmacyControllerTest.java
    └── AdminIntegrationTest.java
```

---

## 4. Design Patterns

### 4.1 Singleton — `AdminDashboardService`

Spring `@Service` enforces a single instance via the IoC container. The dashboard aggregates queries across all repositories — a single managed bean avoids redundant query caching and state inconsistency.

```java
/**
 * Singleton — single instance managed by Spring IoC container.
 * Aggregates KPI data from all repositories for the admin dashboard.
 */
@Service
public class AdminDashboardService {
    // Spring guarantees one instance; no manual singleton boilerplate needed
}
```

---

### 4.2 Observer — Audit Logging

Every admin write action publishes an `AdminActionEvent`. An async listener persists it to `audit_logs` without blocking the HTTP response.

```java
// 1. Event
public class AdminActionEvent extends ApplicationEvent {
    private final Long adminId;
    private final String action;
    private final String targetType;
    private final Long targetId;
    private final String detail;
    // constructor + getters
}

// 2. Publisher (called inside AdminPharmacyService)
applicationEventPublisher.publishEvent(
    new AdminActionEvent(this, adminId, "TOGGLE_STATUS", "PHARMACY", pharmacyId, "active=" + active)
);

// 3. Listener
@Component
public class AuditLogListener {
    @EventListener
    @Async
    public void handle(AdminActionEvent event) {
        AuditLog log = new AuditLog();
        log.setAdminId(event.getAdminId());
        log.setAction(event.getAction());
        log.setTargetType(event.getTargetType());
        log.setTargetId(event.getTargetId());
        log.setDetail(event.getDetail());
        auditLogRepository.save(log);
    }
}
```

> **Note:** Add `@EnableAsync` to the main `@SpringBootApplication` class.

---

### 4.3 Factory Method — `NotificationFactory`

Admin status toggles create the appropriate notification type (Email vs SMS) without the service knowing the concrete class.

```java
public interface Notification {
    void send();
}

public class NotificationFactory {
    public static Notification createNotification(String type, String recipient, String message) {
        if ("EMAIL".equalsIgnoreCase(type)) {
            return new EmailNotification(recipient, message);
        }
        return new SmsNotification(recipient, message);
    }
}

// Usage in AdminPharmacyService
NotificationFactory
    .createNotification("EMAIL", pharmacy.getEmail(), "Your pharmacy status has changed.")
    .send();
```

---

### 4.4 Decorator — `LoggingAdminPharmacyService`

Wraps `AdminPharmacyService` to add request/response logging without modifying business logic. Registered as `@Primary` so it is injected everywhere by default.

```java
@Primary
@Service
public class LoggingAdminPharmacyService implements AdminPharmacyServiceI {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdminPharmacyService.class);
    private final AdminPharmacyService delegate;

    public LoggingAdminPharmacyService(AdminPharmacyService delegate) {
        this.delegate = delegate;
    }

    @Override
    public PharmacySummaryDTO updateStatus(Long id, boolean active, Long adminId) {
        log.info("[ADMIN] updateStatus called — pharmacyId={} active={} by adminId={}", id, active, adminId);
        PharmacySummaryDTO result = delegate.updateStatus(id, active, adminId);
        log.info("[ADMIN] updateStatus completed — result={}", result);
        return result;
    }
    // delegate all other methods similarly
}
```

---

## 5. REST API Specification

**Base path:** `/api/v1/admin/`  
**Auth:** All endpoints require `Authorization: Bearer <JWT>` with `ROLE_ADMIN`

### 5.1 Endpoints

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| `GET` | `/api/v1/admin/users` | 200 | Paginated list of all users |
| `GET` | `/api/v1/admin/users/{id}` | 200 / 404 | Single user by ID |
| `GET` | `/api/v1/admin/pharmacies` | 200 | Paginated list of pharmacies with active flag |
| `GET` | `/api/v1/admin/pharmacies/{id}` | 200 / 404 | Single pharmacy detail |
| `PATCH` | `/api/v1/admin/pharmacies/{id}/status` | 200 / 400 / 404 | Toggle active/inactive |
| `GET` | `/api/v1/admin/dashboard` | 200 | KPI stats |
| `GET` | `/api/v1/admin/audit-logs` | 200 | Paginated admin audit trail |

### 5.2 Request & Response Schemas

**`PATCH /api/v1/admin/pharmacies/{id}/status` — Request**
```json
{
  "active": true
}
```

**`GET /api/v1/admin/dashboard` — Response**
```json
{
  "totalUsers": 342,
  "totalPharmacies": 18,
  "activePharmacies": 15,
  "totalPrescriptions": 1204,
  "prescriptionsToday": 47,
  "treatmentRatePercent": 87.3
}
```

**`GET /api/v1/admin/users` — Response (paginated)**
```json
{
  "content": [
    {
      "id": 1,
      "email": "patient@mail.com",
      "role": "ROLE_PATIENT",
      "createdAt": "2026-01-10T09:00:00Z"
    }
  ],
  "totalElements": 342,
  "totalPages": 35,
  "page": 0,
  "size": 10
}
```

### 5.3 Error Envelope

All errors follow this structure:

```json
{
  "timestamp": "2026-05-20T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Pharmacy with id 99 not found",
  "path": "/api/v1/admin/pharmacies/99"
}
```

Handle in `@RestControllerAdvice`:

| Exception | HTTP Status |
|-----------|-------------|
| `EntityNotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `AccessDeniedException` | 403 |
| `Exception` (fallback) | 500 |

---

## 6. Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AdminSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

**JWT payload must include:**
```json
{
  "sub": "admin@pharmacy.ma",
  "roles": ["ROLE_ADMIN"],
  "iat": 1716200000,
  "exp": 1716286400
}
```

**Security rules:**
- Admin passwords stored as BCrypt hashes (strength 12)
- Every `PATCH /status` call persists an `AuditLog` row via the Observer
- Failed auth attempts log a `WARN` via SLF4J

---

## 7. Step-by-Step Implementation Guide

### Step 1 — Entity & Repository Layer
1. Create `AdminUser.java` with `@OneToOne` to `User` and `@Entity`
2. Create `AuditLog.java` with all fields and `@CreationTimestamp` on `createdAt`
3. Create `AdminUserRepository extends JpaRepository<AdminUser, Long>`
4. Create `AuditLogRepository` with `findAllByOrderByCreatedAtDesc(Pageable p)`
5. Add `active` boolean field to the shared `Pharmacy` entity (default `true`)

### Step 2 — DTO Layer
1. `UserSummaryDTO` — Java record: `id, email, role, createdAt`
2. `PharmacySummaryDTO` — Java record: `id, name, address, active, latitude, longitude`
3. `DashboardStatsDTO` — Java record: all KPI fields as in section 5.2
4. `PharmacyStatusUpdateDTO` — POJO with `@NotNull Boolean active`

### Step 3 — Observer Setup
1. Create `AdminActionEvent extends ApplicationEvent` with fields: `adminId, action, targetType, targetId, detail`
2. Create `AuditLogListener` — `@Component` with `@EventListener @Async void handle(AdminActionEvent e)`
3. Add `@EnableAsync` to main application class

### Step 4 — Factory Setup
1. Create `Notification` interface with `void send()`
2. Create `EmailNotification` and `SmsNotification` implementations
3. Create `NotificationFactory` with `static Notification createNotification(String type, String recipient, String message)`

### Step 5 — Service Layer
1. **`AdminUserService`** — `getAllUsers(Pageable)`, `getUserById(Long)` throwing `EntityNotFoundException`
2. **`AdminPharmacyServiceI`** interface — `getAllPharmacies`, `getPharmacyById`, `updateStatus`
3. **`AdminPharmacyService`** — inject `PharmacyRepository`, `ApplicationEventPublisher`, `NotificationFactory`; in `updateStatus`: fetch → flip flag → save → publish event → send notification
4. **`LoggingAdminPharmacyService`** — `@Primary @Service` decorator wrapping the above
5. **`AdminDashboardService`** — inject all 4 repositories; `getStats()` computes full `DashboardStatsDTO`

### Step 6 — Controller Layer
1. All controllers: `@RestController`, `@RequestMapping("/api/v1/admin")`, `@PreAuthorize("hasRole('ADMIN')")`
2. `AdminUserController` — `GET /users` (pageable), `GET /users/{id}`
3. `AdminPharmacyController` — `GET /pharmacies`, `GET /pharmacies/{id}`, `PATCH /pharmacies/{id}/status`
4. `AdminDashboardController` — `GET /dashboard`
5. Annotate each endpoint with `@Operation(summary="...")` and `@ApiResponse` for Swagger

---

## 8. Test Strategy

### 8.1 Unit Tests (JUnit 5 + Mockito)

> Target: **≥ 80% instruction coverage** on `com.pharmacy.admin` (JaCoCo)

| Test Class | What to Cover |
|------------|---------------|
| `AdminUserServiceTest` | `getAllUsers` returns mapped DTOs; `getUserById` throws 404 for unknown id |
| `AdminPharmacyServiceTest` | `updateStatus` sets flag; publishes event; throws 404 for unknown id |
| `AdminDashboardServiceTest` | `getStats` aggregates values correctly with mock repositories |
| `AuditLogListenerTest` | Listener persists `AuditLog` when event is received |
| `AdminPharmacyControllerTest` | PATCH returns 200; 400 on missing field; 403 without ADMIN role |

### 8.2 Integration Tests (`@SpringBootTest` + H2)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminIntegrationTest {

    @Test void togglePharmacyStatus_shouldReturn200() { ... }
    @Test void getDashboard_shouldReturnStats() { ... }
    @Test void anyAdminEndpoint_withoutJwt_shouldReturn403() { ... }
}
```

`application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 8.3 TDD — 2 Required Features (Red → Green → Refactor)

| Feature | First Test to Write |
|---------|---------------------|
| `togglePharmacyStatus` | Assert service sets `active=false`, publishes event, returns updated DTO |
| `getDashboard` | Assert `DashboardStatsDTO.treatmentRatePercent` equals expected value from mock data |

### 8.4 Postman / Newman

Collection file: `Admin_Workflow.postman_collection.json`

| # | Request | Assertion |
|---|---------|-----------|
| 1 | `POST /auth/login` (pre-request) | Store token in `{{token}}` |
| 2 | `GET /admin/users` | Status 200, `content` is array |
| 3 | `GET /admin/users/1` | Status 200, `id` exists |
| 4 | `GET /admin/pharmacies` | Status 200 |
| 5 | `PATCH /admin/pharmacies/1/status` `{active:false}` | Status 200, `active === false` |
| 6 | `PATCH /admin/pharmacies/1/status` `{active:true}` | Status 200, `active === true` |
| 7 | `GET /admin/dashboard` | Status 200, `treatmentRatePercent` is number |
| 8 | `GET /admin/users` (no auth) | Status 403 |

Run:
```bash
newman run Admin_Workflow.postman_collection.json \
  --reporters cli,json \
  --reporter-json-export newman-report.json
```

---

## 9. Team Task Split

| Member | Component | Deliverable |
|--------|-----------|-------------|
| **Ayman Allouch** | Entity & DB layer | `AdminUser`, `AuditLog` entities, Flyway migration V3, repositories, repo unit tests |
| **Salah-eddin Hamzi** | Service layer + Observer + Factory | All 3 service classes, `AdminActionEvent`, `AuditLogListener`, `NotificationFactory`, TDD on 2 features |
| **Sami Atlagh** | Controller + Security | 3 controllers, `AdminSecurityConfig`, JWT enforcement, `GlobalExceptionHandler` |
| **Walid Abaaqil** | Tests + Docs | Integration tests, Postman collection, Newman run, JaCoCo report, Swagger annotations |

---

## 10. Maven Dependencies

Add to `pom.xml`:

```xml
<!-- Spring Security -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.11.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.11.5</version>
  <scope>runtime</scope>
</dependency>

<!-- Swagger / OpenAPI -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>

<!-- H2 for tests -->
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>test</scope>
</dependency>
```

JaCoCo plugin in `<build><plugins>`:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

Run coverage:
```bash
mvn clean verify
# Report at: target/site/jacoco/index.html
```

---

## 11. Definition of Done

- [ ] All 7 REST endpoints return correct HTTP codes and JSON bodies
- [ ] `ROLE_ADMIN` enforcement tested — 403 on missing/wrong role
- [ ] JaCoCo report ≥ 80% instruction coverage on `admin` package
- [ ] Newman report shows 0 failed tests
- [ ] `AuditLog` row created for every pharmacy status toggle
- [ ] Swagger UI documents all admin endpoints with request/response examples
- [ ] `README.md` explains how to run the admin module in isolation
- [ ] Git history shows atomic commits per feature (TDD Red→Green→Refactor visible)

---

> *INF4121 Printemps 2026 — Ayman Allouch · Sallahedin Hamzi · Sami Atlagh · Walid Abaaqil*