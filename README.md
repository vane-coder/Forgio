# Forgio Backend

Multi-tenant manufacturing management system for small and medium factories in Ghana.
Java Spring Boot + PostgreSQL. This repository contains the **core slice**: authentication,
the multi-tenancy engine, and the production + materials domain — a complete, runnable
foundation you extend feature-by-feature using the same patterns.

---

## What's in this slice

| Layer | Included |
|-------|----------|
| Multi-tenancy | `TenantContext`, `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Auth | register (new factory + manager), login, refresh-token |
| Materials | list / add / update raw materials, low-stock flag |
| Production | submit entry, auto-deduct stock, auto-calc waste + material cost, 24h auto-lock |
| Cross-cutting | global exception handling, Flyway schema, role-based endpoint security |

All other proposal features (departments, permissions, machines/breakdowns, notifications/FCM,
news feed, marketplace, branches/shipments/GPS, weather, Claude AI assistant, reports) have
their **database tables already defined** in the Flyway migration and follow the exact same
patterns shown here.

---

## The multi-tenancy model (single shared schema)

Every company-scoped table carries a `factory_id` column. One factory can **never** read or
write another's data. This is enforced at three layers:

1. **Token** — `factoryId` is embedded in the JWT at login. It is signed, so the client
   cannot alter it.
2. **Filter** — `JwtAuthenticationFilter` reads `factoryId` **from the verified token only**
   (never from the request body or query params) and binds it to a thread-local `TenantContext`.
   The context is cleared in a `finally` block so tenancy never leaks across pooled threads.
3. **Query** — every repository method and service call is scoped by
   `TenantContext.getFactoryId()`. Even if a user guesses another factory's resource ID,
   the `...AndFactory_FactoryId(...)` lookup returns nothing.

```
Login ──> JWT { userId, factoryId, role }
                         │
HTTP request (Bearer) ──▶ JwtAuthenticationFilter
                         │   TenantContext.setFactoryId(tokenFactoryId)
                         ▼
                     Service ── repo.findBy...AndFactory_FactoryId(id, TenantContext.getFactoryId())
                         │
                     finally: TenantContext.clear()
```

---

## Running it

### Prerequisites
- Java 21
- PostgreSQL 14+
- Maven 3.9+

### 1. Create the database
```sql
CREATE DATABASE forgio_db;
CREATE USER forgio WITH PASSWORD 'forgio_secret';
GRANT ALL PRIVILEGES ON DATABASE forgio_db TO forgio;
```

### 2. Set environment variables
```bash
export DB_USERNAME=forgio
export DB_PASSWORD=forgio_secret
export JWT_SECRET="a-long-random-string-at-least-32-characters"
# Optional until you build those slices:
export CLAUDE_API_KEY=...
export OPENWEATHER_API_KEY=...
export GOOGLE_MAPS_API_KEY=...
export FIREBASE_CREDENTIALS_PATH=./firebase-service-account.json
```

### 3. Run
```bash
mvn spring-boot:run
```
Flyway applies `V1__initial_schema.sql` automatically on first boot.

---

## API quick reference (this slice)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | public | Register a factory + its first manager |
| POST | `/api/v1/auth/login` | public | Get access + refresh tokens |
| POST | `/api/v1/auth/refresh-token` | public | Rotate tokens |
| GET  | `/api/v1/materials` | any | List materials (low-stock flagged) |
| POST | `/api/v1/materials` | MANAGER, DEPT_HEAD | Add a material |
| PUT  | `/api/v1/materials/{id}` | MANAGER, DEPT_HEAD | Update stock / cost |
| POST | `/api/v1/production` | any | Submit production (deducts stock, calcs waste + cost) |
| GET  | `/api/v1/production/factory` | MANAGER, DEPT_HEAD | All factory production |
| GET  | `/api/v1/production/me` | any | Own past entries |

See `API_EXAMPLES.md` for full request/response bodies.

---

## How to extend (the repeatable recipe)

To add any remaining feature (e.g. Machines):

1. **Entity** — create `Machine.java` (already done) with a `@ManyToOne Factory factory`.
2. **Repository** — add `findByFactory_FactoryId(UUID)` and
   `findByMachineIdAndFactory_FactoryId(UUID, UUID)`.
3. **DTOs** — request + response records under `dto/`.
4. **Service** — read `TenantContext.getFactoryId()` at the top of every method and pass it
   into the repository. Never trust a `factoryId` from the client.
5. **Controller** — map to `/api/v1/...`, guard writes with `@PreAuthorize`.

Follow `MaterialService` as the reference implementation — it is the smallest complete example
of the pattern.

---

## Project structure

```
src/main/java/com/forgio/
├── config/        SecurityConfig
├── security/      TenantContext, JwtTokenProvider, JwtAuthenticationFilter
├── entity/        JPA entities (all proposal tables)
├── repository/    Spring Data repositories (tenant-scoped finders)
├── service/       AuthService, MaterialService, ProductionService, lock scheduler
├── controller/    REST controllers (/api/v1)
├── dto/           request/ + response/ records
├── exception/     GlobalExceptionHandler + custom exceptions
└── enums/         UserRole, MachineStatus, ShipmentStatus, ...
src/main/resources/
├── application.properties
└── db/migration/  V1__initial_schema.sql  (Flyway)
```
-- Database connection details:
-- Host: localhost
-- Port: 5432
-- Database: forgio_db
-- Username: your_user
-- Password: your_password
