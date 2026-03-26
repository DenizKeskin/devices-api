# Devices API

A REST API for persisting and managing device resources, built with Java 21 and Spring Boot.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct |
| Documentation | SpringDoc OpenAPI (Swagger) |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build | Maven 3.9+ |

---

## Prerequisites

- Docker & Docker Compose
- Java 21+ (for local development)
- Maven 3.9+ (for local development)

---

## Running the Application

### With Docker (recommended)

```bash
mvn clean package -DskipTests
docker-compose up --build
```

The application will be available at `http://localhost:8080`.

### Local Development

Start only the database:

```bash
docker-compose up db
```

Then run the application:

```bash
mvn spring-boot:run
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/devices` | Create a new device |
| `GET` | `/api/v1/devices` | List all devices (supports pagination & filtering) |
| `GET` | `/api/v1/devices/{id}` | Get a device by ID |
| `PUT` | `/api/v1/devices/{id}` | Fully update a device |
| `PATCH` | `/api/v1/devices/{id}` | Partially update a device |
| `DELETE` | `/api/v1/devices/{id}` | Delete a device |

### Filtering & Pagination

```
GET /api/v1/devices?brand=Samsung
GET /api/v1/devices?state=AVAILABLE
GET /api/v1/devices?page=0&size=10&sort=brand,asc
```

---

## Domain

### Device States

| State | Description |
|---|---|
| `AVAILABLE` | Device is available for use |
| `IN_USE` | Device is currently in use |
| `INACTIVE` | Device is inactive |

### Business Rules

- Creation time is set automatically and **cannot be updated**
- `name` and `brand` **cannot be updated** if the device is `IN_USE`
- `IN_USE` devices **cannot be deleted**
- Optimistic locking is enforced — `version` field must be provided on update/patch requests

---

## Example Requests

### Create a device

```bash
curl -X POST http://localhost:8080/api/v1/devices \
  -H "Content-Type: application/json" \
  -d '{"name": "Galaxy S24", "brand": "Samsung"}'
```

### Fully update a device

```bash
curl -X PUT http://localhost:8080/api/v1/devices/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Galaxy S25", "brand": "Samsung", "state": "IN_USE", "version": 0}'
```

### Partially update a device

```bash
curl -X PATCH http://localhost:8080/api/v1/devices/1 \
  -H "Content-Type: application/json" \
  -d '{"state": "INACTIVE", "version": 1}'
```

### Filter by brand

```bash
curl http://localhost:8080/api/v1/devices?brand=Samsung
```

### Delete a device

```bash
curl -X DELETE http://localhost:8080/api/v1/devices/1
```

---

## Monitoring

Spring Boot Actuator endpoints are available at:

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application and DB health status |
| `/actuator/info` | Application info |
| `/actuator/metrics` | JVM, HTTP, DB connection pool metrics |

---

## Running Tests

```bash
mvn test
```

> Testcontainers is used for integration tests — Docker must be running.

Test coverage is **92%**, measured with JaCoCo:

```bash
mvn verify
open target/site/jacoco/index.html
```

---

## CI/CD

GitHub Actions is configured to automatically run all tests on every push and pull request to `main`.

The workflow file is located at `.github/workflows/ci.yml`.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `devicesdb` | Database name |
| `DB_USER` | `devices_user` | Database user |
| `DB_PASSWORD` | `devices_pass` | Database password |
| `SERVER_PORT` | `8080` | Application port |

---

## Future Improvements

- Add authentication and authorization (e.g. Spring Security + JWT)
- Add rate limiting
- Add MDC / correlation ID for distributed tracing
- Add soft delete support
- Expose metrics to Prometheus + Grafana
