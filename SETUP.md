# PharmAdmin — Setup & Running Guide

Complete step-by-step instructions to get the application running locally from a clean machine.

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17 | https://adoptium.net |
| Apache Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |
| Git | Any | https://git-scm.com |

Verify your installation:

```bash
java -version        # openjdk 17.x.x
mvn -version         # Apache Maven 3.9.x
docker -version      # Docker version 24+
docker compose version
```

---

## 1. Clone the Repository

```bash
git clone https://github.com/LUZAmaxxp/pharmaSUIIIIII.git
cd pharmaSUIIIIII
```

---

## 2. Start the Database (Docker)

The database runs in a Docker container — no local MySQL installation required.

```bash
docker compose up -d
```

This starts a **MySQL 8.0** container with:
- Host port: `3306`
- Database: `pharmacy_db`
- Username: `root`
- Password: *(empty)*
- Data persisted in a Docker named volume (`pharmacy_db_data`)

**Verify the container is healthy:**

```bash
docker compose ps
```

Expected output:
```
NAME                 STATUS
pharmaSUIIIIII-db-1  Up (healthy)
```

> The app will not start if the DB container is not healthy. Wait ~15 seconds after `docker compose up` before running the app.

**Stop the database:**

```bash
docker compose down          # stop container, keep data
docker compose down -v       # stop container AND delete all data
```

---

## 3. Configure Environment Variables

Export the required environment variables in your terminal session:

```bash
export JAVA_HOME="/path/to/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export PATH="/path/to/apache-maven/bin:$PATH"
```

**Windows (Git Bash) example:**
```bash
export JAVA_HOME="/c/Program Files/Java/jdk-17"
export PATH="/c/Users/<you>/apache-maven-3.9.9/bin:$PATH"
```

Verify:
```bash
java -version && mvn -version
```

---

## 4. Install Dependencies

Maven downloads all dependencies automatically on first build. Run from the project root:

```bash
mvn dependency:resolve
```

Dependencies include:
- Spring Boot 3.2.5 (Web, Security, Data JPA, Validation)
- MySQL Connector/J 8.3.0
- Flyway 9.22.3 (database migrations)
- JJWT 0.11.5 (JWT authentication)
- Springdoc OpenAPI 2.3.0 (Swagger UI)
- JaCoCo 0.8.11 (test coverage)
- H2 (in-memory DB for tests only)

---

## 5. Run the Application

```bash
mvn spring-boot:run
```

Spring Boot will automatically:
1. Connect to the MySQL Docker container
2. Run Flyway migrations (V1 → V2 → V3) to create and seed all tables
3. Start the embedded Tomcat server on **port 8080**

**Successful startup output:**
```
Flyway Community Edition 9.22.3
Successfully validated 3 migrations
Schema `pharmacy_db` is up to date. No migration necessary.
Started PharmacyApplication in ~6 seconds
Tomcat started on port 8080
```

---

## 6. Access the Application

| URL | Description |
|-----|-------------|
| http://localhost:8080 | Admin dashboard (login page) |
| http://localhost:8080/swagger-ui.html | Interactive API documentation |
| http://localhost:8080/v3/api-docs | Raw OpenAPI JSON |

### Admin Login Credentials

| Field | Value |
|-------|-------|
| Email | `admin@pharmacy.ma` |
| Password | `Admin@1234` |

The login returns a **JWT token** stored in `localStorage`. All subsequent API calls use this token in the `Authorization: Bearer <token>` header.

---

## 7. Run Tests

Tests use an **H2 in-memory database** by default — no Docker required.

```bash
mvn test
```

To also enforce JaCoCo coverage thresholds (≥ 80% instructions, ≥ 70% branches):

```bash
mvn verify
```

**Coverage report** (generated after `mvn verify`):
```
target/site/jacoco/index.html
```

Open it in a browser to see line-by-line coverage.

---

## 8. REST API Quick Reference

All admin endpoints require the `Authorization: Bearer <token>` header.

### Authentication

```
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "admin@pharmacy.ma", "password": "Admin@1234" }
```

Response:
```json
{ "token": "<jwt>" }
```

### Dashboard

```
GET /api/v1/admin/dashboard
```

### Pharmacies

```
GET    /api/v1/admin/pharmacies              # paginated list
PATCH  /api/v1/admin/pharmacies/{id}/status  # activate / deactivate
```

### Users

```
GET /api/v1/admin/users                      # paginated list
```

---

## 9. Database Migrations

Flyway runs migrations automatically on startup. Migration files are in:

```
src/main/resources/db/migration/
  V1__init_schema.sql    — creates users, pharmacies, prescriptions, orders tables
  V2__seed_admin.sql     — inserts the default admin user
  V3__admin_schema.sql   — adds active flag to pharmacies, creates admin_users & audit_logs
```

If the database is clean (no tables), all three migrations run in order.

**To reset the database completely:**

```bash
docker compose down -v      # wipes all data
docker compose up -d        # fresh container
mvn spring-boot:run         # Flyway re-runs all migrations
```

---

## 10. CI / CD Pipeline

Every push to any branch and every pull request targeting `main` triggers the GitHub Actions pipeline defined in `.github/workflows/ci.yml`.

The pipeline:
1. Spins up a MySQL 8.0 service container
2. Runs `mvn verify` — compile → test against real MySQL → enforce coverage
3. Uploads the JaCoCo HTML report as a build artifact (kept 7 days)

**View pipeline runs:**
```
https://github.com/LUZAmaxxp/pharmaSUIIIIII/actions
```

A PR to `main` is blocked if any test fails or coverage drops below the thresholds.

---

## 11. Common Issues

### Port 3306 already in use

Another MySQL instance (e.g. WAMP) is running on the same port. Stop it first:

```bash
# Windows — stop WAMP MySQL service
net stop wampmysqld64

# then start Docker
docker compose up -d
```

### `docker compose up` hangs or container is unhealthy

MySQL takes ~15–30 seconds to initialize on first run. Wait for:
```bash
docker compose ps   # STATUS should show "healthy"
```

### `java.net.ConnectException: Connection refused` on port 3306

The Docker container is not running. Start it:
```bash
docker compose up -d
```

### Tests fail locally with `DataAccessException`

Tests use H2 in-memory and should never need Docker. If they fail, ensure no environment variables like `SPRING_DATASOURCE_URL` are set in your shell that would override the H2 config.

### `Flyway checksum mismatch` on restart

If a migration file was edited after it was applied, add `validate-on-migrate: false` to `application.yml` (already set) and restart. Flyway will skip checksum validation.

---

## Project Structure (Quick Reference)

```
pharmaSUIIIIII/
├── docker-compose.yml                  ← MySQL container definition
├── .github/workflows/ci.yml            ← CI pipeline
├── pom.xml                             ← Maven build & dependencies
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/
│   │   │   ├── PharmacyApplication.java
│   │   │   └── admin/                  ← all admin feature code
│   │   └── resources/
│   │       ├── application.yml         ← main config (MySQL, JWT, Flyway)
│   │       ├── db/migration/           ← Flyway SQL migrations
│   │       └── static/index.html       ← admin dashboard SPA
│   └── test/
│       ├── java/com/pharmacy/admin/    ← unit + integration tests
│       └── resources/
│           └── application-test.yml    ← test config (H2 / env-var overridable)
```
