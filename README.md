# 🏢 CDAC Enterprise Application & Admission Management System

A **production-grade** Spring Boot 3 REST API backend for managing institutional applications, courses, admissions, user authentication, document uploads, and audit logging — with a **beautiful interactive frontend** for API testing.

---

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT-based authentication** with access tokens (15 min expiry)
- **Role-based access control** (`ROLE_STUDENT`, `ROLE_ADMIN`)
- Secure password hashing with **BCrypt**
- Public registration + admin seeding on startup

### 📚 Course Management
- Full CRUD for courses (admin only)
- Soft-delete support (`deleted` flag)
- Active/inactive course state
- Search by name with pagination

### 📋 Application Management
- Students can submit applications for active courses
- Duplicate application prevention (one per student per course)
- Admin review workflow: SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED
- Statement of purpose and admin remarks support

### 📄 Document Upload
- File upload with content-type validation (PDF, JPEG, PNG, DOC, DOCX)
- 5MB file size limit
- Virus-safe storage with UUID filenames
- Path traversal protection

### 🔔 Notifications
- Automatic notifications on application status changes
- Read/unread tracking
- Paginated retrieval with unread filter

### 📊 Admin Dashboard
- Aggregate statistics: users, courses, applications, documents
- Type-filtered application counts

### 📜 Audit Logging
- Automatic audit trail for all critical actions
- Actor tracking with IP address capture
- Paginated log retrieval

### 🩺 Health Check
- Simple health endpoint for monitoring
- Spring Boot Actuator probes (production)

### 🌐 Interactive Frontend
- **Dark theme** with gradient accents and animations
- **Responsive** design (mobile-friendly)
- **Live API testing** via browser
- Real-time **dashboard stats** visualization
- Course search, application review, notification viewer
- Pagination throughout

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.x |
| **Security** | Spring Security, JWT (jjwt 0.12.x) |
| **Database** | MySQL 8.0, Spring Data JPA, Hibernate |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Validation** | Jakarta Bean Validation |
| **File Upload** | Multipart file upload |
| **Build** | Maven, Docker |
| **Frontend** | Vanilla HTML, CSS (modern dark UI), vanilla JS |
| **Monitoring** | Spring Boot Actuator |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (JDK)
- **MySQL 8.0+** (running on port 3306)
- **Maven** (or use the included `mvnw` wrapper)

### 1. Clone & Configure

```bash
git clone https://github.com/cdac/enterprise.git
cd enterprise
```

### 2. Create Database

```sql
CREATE DATABASE IF NOT EXISTS cdac_enterprise_db;
```

### 3. Configure `application.properties`

Edit `src/main/resources/application.properties` or use environment variables:

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/cdac_enterprise_db` | MySQL JDBC URL |
| `spring.datasource.username` | `root` | DB username |
| `spring.datasource.password` | `root` | DB password |
| `app.jwt.secret` | (see file) | JWT signing key (min 256-bit) |
| `app.jwt.expiration-ms` | `900000` | Token expiry in ms |

### 4. Run the Application

```bash
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run
./mvnw package -DskipTests
java -jar target/enterprise-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 5. Access the Application

| Resource | URL |
|----------|-----|
| **Interactive Frontend** | [http://localhost:8080](http://localhost:8080) |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **API Docs (JSON)** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

---

## 🐳 Docker Deployment

```bash
# Start with MySQL + Backend
docker compose up -d --build

# Check logs
docker compose logs -f backend

# Stop
docker compose down
```

The Docker setup will:
- Create a MySQL 8.0 container (port 3307)
- Build and run the Spring Boot app (port 8080)
- Auto-configure the database connection

---

## 🔑 Default Credentials

After startup, the system seeds:

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@cdac.local` | `Admin@123` |

---

## 📡 API Endpoints

### Public Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/health` | Health check |
| `POST` | `/api/v1/auth/register` | Register new student |
| `POST` | `/api/v1/auth/login` | Login |
| `GET` | `/api/v1/courses` | List active courses |
| `GET` | `/api/v1/courses/search?name=` | Search active courses |

### Student Endpoints (requires `ROLE_STUDENT`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/applications` | Submit application |
| `GET` | `/api/v1/applications` | My applications |
| `GET` | `/api/v1/applications/{id}` | My application detail |
| `POST` | `/api/v1/documents/upload` | Upload document |
| `GET` | `/api/v1/documents` | My documents |
| `GET` | `/api/v1/notifications` | My notifications |
| `PATCH` | `/api/v1/notifications/{id}/read` | Mark read |

### Admin Endpoints (requires `ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/admin/courses` | Create course |
| `PUT` | `/api/v1/admin/courses/{id}` | Update course |
| `DELETE` | `/api/v1/admin/courses/{id}` | Soft delete course |
| `GET` | `/api/v1/admin/courses` | List all courses |
| `GET` | `/api/v1/admin/courses/{id}` | Get course by ID |
| `GET` | `/api/v1/admin/applications` | List all applications |
| `GET` | `/api/v1/admin/applications/{id}` | Get application |
| `GET` | `/api/v1/admin/applications/status/{status}` | Filter by status |
| `PUT` | `/api/v1/admin/applications/{id}/review` | Review application |
| `GET` | `/api/v1/admin/applications/{id}/documents` | View app documents |
| `GET` | `/api/v1/admin/dashboard/stats` | Dashboard stats |
| `GET` | `/api/v1/admin/audit-logs` | Audit logs |
| `GET` | `/api/v1/admin/users` | List users |
| `GET` | `/api/v1/admin/users/{id}` | Get user by ID |
| `DELETE` | `/api/v1/admin/users/{id}` | Soft delete user |
| `PUT` | `/api/v1/admin/users/{id}` | Update user |
| `GET` | `/api/v1/admin/users/search?email=` | Search users |

---

## 🧪 Testing via Swagger

1. Start the application
2. Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
3. **Register/Login** via `/api/v1/auth/login`
4. Copy the `accessToken` from the response
5. Click **Authorize** (top-right) and paste: `Bearer <token>`
6. Try any authenticated endpoint directly from Swagger UI

### Quick Test Flow

```bash
# 1. Health check
curl http://localhost:8080/api/v1/health

# 2. Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@test.com","password":"Test@1234","phoneNumber":"9876543210"}'

# 3. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cdac.local","password":"Admin@123"}'

# 4. Get courses (public)
curl http://localhost:8080/api/v1/courses

# 5. Admin: create course (with token)
curl -X POST http://localhost:8080/api/v1/admin/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"code":"CDAC-DAC","name":"Post Graduate Diploma in Advanced Computing","description":"9-month intensive program","durationInMonths":9,"fee":150000,"capacity":120,"active":true}'
```

---

## 📦 Project Structure

```
enterprise/
├── docker-compose.yml          # Docker Compose for MySQL + backend
├── Dockerfile                   # Multi-stage Docker build
├── pom.xml                      # Maven project descriptor
├── src/
│   ├── main/
│   │   ├── java/com/cdac/enterprise/
│   │   │   ├── config/          # OpenAPI, CORS, DataInitializer, WebConfig
│   │   │   ├── constant/        # Enums and constants (status, roles, messages)
│   │   │   ├── controller/      # REST controllers (public, student, admin)
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Custom exceptions + global handler
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/        # JWT auth, filters, entry points
│   │   │   └── service/         # Business logic (interfaces + impls)
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html   # ✨ Interactive frontend
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── logback-spring.xml
│   └── test/
└── uploads/documents/           # File upload storage
```

---

## 🔐 Security Architecture

- **Stateless JWT authentication** — no session storage
- **Password hashing** via BCrypt (strength: 10)
- **Method-level security** via `@PreAuthorize` annotations
- **Custom exception handlers** for auth failures
- **CSRF disabled** (stateless API)
- **CORS configurable** for frontend domains
- **Path traversal protection** for file uploads
- **Soft delete** pattern prevents data loss

---

## 🧹 Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw verify
```

---

## 📈 Production Considerations

1. **Change the JWT secret** to a secure random key (min 256-bit)
2. **Use environment variables** for all secrets (`SPRING_PROFILES_ACTIVE=prod`)
3. **Enable HTTPS** with a reverse proxy (Nginx, Traefik)
4. **Set up database backups** for MySQL
5. **Configure rate limiting** on the reverse proxy
6. **Monitor with Actuator** endpoints (`/actuator/health`, `/actuator/metrics`)
7. **Set `spring.jpa.hibernate.ddl-auto=validate`** in production
8. **Use a proper secrets manager** for credentials

---

## 📄 License

[MIT](LICENSE)

---

## 👥 Contributors

- **CDAC Enterprise Backend Team** — [support@cdac-enterprise.local](mailto:support@cdac-enterprise.local)
