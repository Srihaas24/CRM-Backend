# 🏢 Multi-Tenant CRM & Task Management REST API

[![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6-green?logo=springsecurity)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)](https://www.docker.com/)
[![Swagger Docs](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger)](https://backend-app-qcgj.onrender.com/swagger-ui/index.html#/)
[![Deployed on Render](https://img.shields.io/badge/Render-Live%20API-46E3B7?logo=render)](https://backend-app-qcgj.onrender.com/swagger-ui/index.html#/)

A production-grade, multi-tenant **Customer Relationship & Project/Task Management RESTful Backend API** built with **Java 21** and **Spring Boot**. 

Engineered with enterprise security standards, featuring **stateless JWT authentication with Redis-backed token denylisting**, strict **multi-tenant data isolation** at the JPA query layer, dynamic filtering using **Spring Data JPA Specifications**, and containerized multi-service deployment via **Docker Compose**.

---

## 🚀 Live Interactive API Documentation

> **Interactive Swagger UI:** [https://backend-app-qcgj.onrender.com/swagger-ui/index.html#/](https://backend-app-qcgj.onrender.com/swagger-ui/index.html#/)

Recruiters and engineers can test every endpoint directly in the browser via Swagger UI:
1. Open the **[Live Swagger UI](https://backend-app-qcgj.onrender.com/swagger-ui/index.html#/)**.
2. Create an organization via `POST /api/v1/auth/register/organization` or authenticate via `POST /api/v1/auth/login`.
3. Copy the returned `accessToken`.
4. Click the **Authorize** button at the top-right of Swagger UI and enter `Bearer <your_token>`.
5. Execute requests across Projects, Tasks, Users, and Team Members.

---

## 🏛️ System Architecture

```text
                        ┌─────────────────────────────────────────┐
                        │          Client / Swagger UI            │
                        └────────────────────┬────────────────────┘
                                             │ HTTP Requests (Bearer JWT)
                                             ▼
                        ┌─────────────────────────────────────────┐
                        │        Spring Security 6 Filter         │
                        │   (JwtAuthFilter + Redis Denylist Check)│
                        └────────────────────┬────────────────────┘
                                             │ Validated Request
                                             ▼
                        ┌─────────────────────────────────────────┐
                        │           REST Controllers              │
                        │    (Auth, User, Project, Task, Org)     │
                        └────────────────────┬────────────────────┘
                                             │
                                             ▼
                        ┌─────────────────────────────────────────┐
                        │             Service Layer               │
                        │     (Business Logic & Tenant Rules)     │
                        └──────────────┬──────────────────┬───────┘
                                       │                  │
                   JPA Criteria API /  │                  │ Cache / Denylist /
                   Specifications      ▼                  ▼ Refresh Rotation
                        ┌───────────────────────┐  ┌───────────────────────┐
                        │     PostgreSQL 17     │  │        Redis 7        │
                        │  (Tenants, Projects,  │  │   (Token Denylist,    │
                        │    Tasks, Audit Logs) │  │    Refresh Tokens)    │
                        └───────────────────────┘  └───────────────────────┘
```

---

## 🌟 Key Engineering Features

### 1. 🏢 Strict Multi-Tenancy & Data Isolation
* **Organization Scoping:** Every user, project, and task strictly belongs to an `Organization`.
* **JPA Criteria Predicates:** Queries dynamically enforce tenant boundaries using `cb.equal(root.get("project").get("organization").get("id"), currentUser.getOrganization().getId())` inside custom Spring Data JPA Specifications.
* **Zero Data Bleed:** Prevents cross-tenant access even if external entity IDs are guessed.

### 2. 🔐 Advanced Security & Session Invalidation
* **Stateless JWT + Refresh Token Rotation:** Issues short-lived access tokens (30 minutes) alongside UUID-based refresh tokens (7 days).
* **Redis Token Denylist / Instant Logout:** Overcomes the classic limitation of stateless JWTs (inability to revoke before expiry) by recording revoked access tokens in **Redis** with automated TTL expiration.
* **Instant User-Wide Session Termination:** When an account is deactivated or compromised, all active sessions are instantly blocked via a Redis user denylist prefix.
* **Granular RBAC:** Role-Based Access Control protecting routes across `ADMIN`, `PROJECT_MANAGER`, `DEVELOPER`, and `CLIENT`.

### 3. 🔍 Dynamic Multi-Attribute Filtering (JPA Specifications)
* Implemented dynamic filtering for tasks and users without boiler-plate repository queries.
* Query tasks simultaneously by `projectId`, `assignedUserId`, and `taskStatus`, coupled with subqueries ensuring developers only view tasks within projects they are assigned to.

### 4. 📬 Asynchronous Onboarding & Security Auditing
* **Invite-Based User Onboarding:** Organizations send cryptographically secure invitation tokens via email for streamlined team joining.
* **Password Reset Pipeline:** Time-sensitive reset tokens dispatched via email.
* **Audit Logging (`audit_logs`):** Automatically tracks security-sensitive events (logins, failed attempts, password resets, IP addresses, and timestamps).

### 5. 🐳 Production Containerization & DevOps
* Multi-container setup with **Docker** and **Docker Compose**.
* Orchestrates the Spring Boot application, PostgreSQL 17 database, and Redis 7 with integrated health checks (`pg_isready`, `redis-cli ping`).
* Production-ready deployment hosted on **Render**.

---

## 🛠️ Tech Stack

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Java 21 | Modern LTS Java featuring record types and pattern matching |
| **Framework** | Spring Boot 3.4+ | Backend REST API framework |
| **Security** | Spring Security 6 + JJWT | Stateless authentication, authorization & filters |
| **Primary Database** | PostgreSQL 17 | Relational persistence with JPA / Hibernate |
| **In-Memory Store** | Redis 7 | Refresh token store, session invalidation denylist |
| **Query Engine** | Spring Data JPA Specifications | Dynamic Criteria API filtering & tenant predicates |
| **Containerization** | Docker & Docker Compose | Multi-container environment and deployment |
| **Documentation** | OpenAPI 3 / Swagger UI | Interactive API documentation and testing |
| **Build Tool** | Apache Maven | Dependency management and build packaging |
| **Hosting** | Render | Cloud platform hosting live API instance |

---

## 📋 Core API Endpoints

### Authentication & Account
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register/organization` | Register a new organization and root admin | Public |
| `POST` | `/api/v1/auth/login` | Authenticate and receive Access + Refresh tokens | Public |
| `POST` | `/api/v1/auth/refresh` | Rotate expired Access Token with Refresh Token | Public |
| `POST` | `/api/v1/auth/logout` | Revoke session and invalidate tokens in Redis | Authenticated |
| `POST` | `/api/v1/auth/forgot-password` | Initiate password reset email pipeline | Public |
| `POST` | `/api/v1/auth/reset-password` | Complete password reset via verification token | Public |
| `GET` | `/api/v1/auth/verify-email` | Verify registered organization email address | Public |

### User Management & Team Invitations
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/invite` | Send email invitation with role to new team member | Admin / PM |
| `POST` | `/api/v1/auth/accept-invite` | Set password and activate invited user profile | Public |
| `GET` | `/api/v1/users` | List users with dynamic filters and pagination | Admin / PM |
| `PATCH` | `/api/v1/users/{id}/deactivate` | Deactivate user and trigger instant Redis session cutoff | Admin |

### Projects & Task Management
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/projects` | Create a new organization project | Admin / PM |
| `GET` | `/api/v1/projects` | Retrieve organization projects (filtered by membership) | Authenticated |
| `POST` | `/api/v1/projects/{id}/members` | Assign team members to a project | Admin / PM |
| `POST` | `/api/v1/tasks` | Create task with priority, status, and assignee | Admin / PM |
| `GET` | `/api/v1/tasks` | Dynamically query tasks by status, user, and project | Authenticated |
| `PUT` | `/api/v1/tasks/{id}` | Update task status, priority, or assignee | Member / PM |

---

## 💻 Local Setup & Installation

### Prerequisites
* **Java 21 JDK**
* **Maven 3.9+**
* **Docker & Docker Compose** (optional, for containerized run)

### Method 1: Run with Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/Srihaas24/CRM-Backend.git
   cd CRM-Backend
   ```

2. Create an environment file:
   ```bash
   cp .env.example .env
   ```
   *(Populate database credentials, Redis password, and mail configuration in `.env`)*

3. Start all services:
   ```bash
   docker compose up --build
   ```

4. The application will be running at `http://localhost:8080`.
   Access Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

---

### Method 2: Run Locally with Maven

1. Ensure local PostgreSQL (port `5432`) and Redis (port `6379`) are running.
2. Configure your `src/main/resources/application.properties` or environment variables:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/CRM
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=your_password
   SPRING_DATA_REDIS_HOST=localhost
   SPRING_DATA_REDIS_PORT=6379
   JWT_SECRET=your_base64_or_hex_encoded_secret_key
   ```
3. Build and launch:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## 👤 Author

**Iruventi Srihaas**
* **GitHub:** [@Srihaas24](https://github.com/Srihaas24)
* **LinkedIn:** [Iruventi Srihaas](https://www.linkedin.com/in/srihaas-iruventi/)
* **Email:** srihaas24@gmail.com
