# 🏨 Tourism Hotel Booking System

**A modular Spring Boot backend for comprehensive hotel reservation and management**

---

## Overview

The Tourism Hotel Booking System is a well-structured modular Spring Boot REST API backend designed for comprehensive hotel reservations, room management, availability tracking, dynamic pricing, secure payments, and guest notifications. Built as a modular monolith with stateless JWT authentication, the system demonstrates clean separation of concerns and is structured for future evolution into microservices.

This project exemplifies backend engineering best practices with distinct module boundaries, security-first design, practical API documentation via Swagger, and comprehensive test coverage. It is suitable for academic submission and understanding of enterprise REST API patterns.

---

## ✨ Features

- **Hotel & Room Management** — Define hotels, room types, capacities, and base pricing with flexible amenity assignments
- **Advanced Availability & Pricing** — Real-time room availability checking with dynamic pricing rules (day-of-week multipliers for weekends)
- **Booking Lifecycle** — Complete booking creation, retrieval, modification, and cancellation with inventory management
- **Mock Payment Processing** — Stateless payment endpoint for order confirmation without external integrations
- **Guest Notifications** — Email notification simulation for booking confirmations, cancellations, and updates
- **Amenity Management** — Hotel-level and room-level amenities with scope-based validation (HOTEL, ROOM_TYPE, BOTH)
- **JWT Authentication** — Stateless token-based security with role-based access control (GUEST, MANAGER, ADMIN)
- **REST API with Swagger** — OpenAPI 3.0 documentation with Bearer token authentication support for live testing
- **Comprehensive Testing** — Unit tests for services and mappers; integration tests for end-to-end booking flows

---

## 📦 Core Modules & Architecture

The system is organized as a **modular monolith** with clear separation of concerns:

### Authentication & Security
- **`auth`** — Registration, login, and JWT token generation
- **`security`** — Spring Security configuration, stateless session policy, JWT filter chain
- **`user`** — User entity, roles (GUEST, MANAGER, ADMIN), and repository

### Catalog Management
- **`catalog`** — Domain modules for hotels, room types, and amenities
  - *`catalog.hotel`* — Hotel entity with amenity associations
  - *`catalog.room`* — RoomType entity, capacity, and pricing
  - *`catalog.amenity`* — Amenity features, types, and scope validation

### Booking Operations
- **`availabilityPricing`** — Availability checking, pricing calculations, and dynamic multipliers
- **`booking`** — Booking entity, service layer, order management, and cancellation workflows
- **`payment`** — Mock payment processing and order confirmation logic

### Cross-Cutting Concerns
- **`notification`** — Email notification service and templates
- **`common`** — Centralized exception handling, Swagger configuration, custom exceptions

### Infrastructure
- **`util`** — JWT testing utilities, helper functions

---

## ⚙️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Framework** | Spring Boot 3.2.0 |
| **Language** | Java 21 |
| **Persistence** | JPA/Hibernate, MySQL (production), H2 (testing) |
| **Security** | Spring Security 6.x, JWT (JJWT 0.12.3) |
| **API Documentation** | Springdoc OpenAPI 2.3.0 (Swagger UI) |
| **Validation** | Jakarta Validation API |
| **Testing** | JUnit 5, Mockito, Spring Test |
| **Build Tool** | Apache Maven 3.9+ |
| **Database Drivers** | MySQL Connector/J (MySQL), H2 (in-memory tests) |

---

## 🔐 Security

The system implements **stateless JWT-based authentication** following REST API best practices:

- **Stateless Sessions** — No server-side session state; each request includes a self-contained JWT token
- **Token-Based Auth** — JWT tokens generated on `/auth/register` and `/auth/login` include role and user ID claims
- **Bearer Token Scheme** — Protected endpoints require `Authorization: Bearer <token>` header
- **Role-Based Access Control** — Endpoint protection via Spring Security with role annotations (ROLE_ADMIN, ROLE_MANAGER, ROLE_GUEST)
- **Password Encoding** — BCrypt hashing for stored passwords; constant-time comparison during login
- **Filter Chain** — Custom `JwtAuthFilter` validates tokens before controller execution
- **CSRF Protection** — Disabled for REST API; CORS configuration via Spring Security

**Note:** This project uses JWT for demonstration and learning purposes. In production or academic extensions, consider additional hardening such as token refresh flows, short expiration times, HTTPS enforcement, and secure secret management.

---

## 🧪 Testing

The project includes **comprehensive unit and integration tests** covering:

- **Unit Tests** — Service layer business logic, mappers, and utility functions
- **Integration Tests** — End-to-end booking flows, auth flows, amenity validation
- **Test Isolation** — H2 in-memory database (`spring.profiles.active=test`) ensures test independence
- **JWT in Tests** — `BaseIntegrationTest` provides token utilities for authenticated endpoint testing
- **Test Profile** — Distinct configuration (`application-test.properties`) separates test from production

Run tests with:
```bash
mvn clean test
```

---

## 📁 Project Structure

```
tourism-booking/
├── pom.xml                                  # Maven configuration
├── src/
│   ├── main/java/com/youruni/tourismbooking/
│   │   ├── auth/                            # Authentication service & controller
│   │   ├── booking/                         # Booking service, entity, mapper
│   │   ├── catalog/
│   │   │   ├── hotel/                       # Hotel management
│   │   │   ├── room/                        # Room type management
│   │   │   └── amenity/                     # Amenity service & validation
│   │   ├── availabilityPricing/             # Availability & pricing logic
│   │   ├── payment/                         # Mock payment processing
│   │   ├── notification/                    # Email notifications
│   │   ├── security/                        # Spring Security, JWT, filters
│   │   ├── user/                            # User entity & roles
│   │   ├── common/                          # Exception handling, Swagger config
│   │   ├── util/                            # Helper utilities
│   │   └── TourismBookingApplication.java   # Spring Boot entry point
│   ├── main/resources/
│   │   └── application.properties           # Main configuration
│   ├── test/java/com/youruni/tourismbooking/
│   │   ├── *Test.java                       # Unit tests
│   │   ├── integration/                     # Integration tests
│   │   └── config/                          # Test configuration & base classes
│   └── test/resources/
│       └── application-test.properties      # Test profile configuration
└── target/                                  # Maven build output
```

---

## 🚀 How to Run

### Prerequisites
- **Java 21** or later
- **Maven 3.9+**
- **MySQL 8.0+** (for production) or **H2** (automatic for tests)
- **Git** (to clone the repository)

### Setup & Execution

#### 1. Clone & Build
```bash
git clone <repository-url>
cd tourism-booking
mvn clean install
```

#### 2. Configure Database (Production)
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tourism_booking?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=<your-password>
```

Create the database:
```sql
CREATE DATABASE tourism_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3. Run the Application
```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

#### 4. Run Tests
```bash
mvn clean test              # Unit & integration tests on H2
mvn verify                  # Full Maven verify phase
```

---

## 📌 API Documentation & Swagger

### Accessing Swagger UI
Once the application is running, visit:
```
http://localhost:8080/swagger-ui.html
```

### Authentication in Swagger
1. **Register** a new user: `POST /auth/register`
   ```json
   {
     "username": "guest_user",
     "email": "user@example.com",
     "fullName": "Guest User",
     "password": "SecurePassword@123"
   }
   ```

2. **Login** to obtain JWT: `POST /auth/login`
   ```json
   {
     "usernameOrEmail": "guest_user",
     "password": "SecurePassword@123"
   }
   ```

3. **Authorize** in Swagger UI:
   - Click the **Authorize** button (top right)
   - Paste the `token` from the login response with prefix: `Bearer <token>`
   - Click **Authorize** and close the dialog

4. **Test Protected Endpoints** — All authenticated endpoints now include your JWT token automatically

### Key Endpoints

| Endpoint | Method | Auth Required | Purpose |
|----------|--------|---------------|---------|
| `/auth/register` | POST | No | Create new user account |
| `/auth/login` | POST | No | Authenticate and receive JWT |
| `/api/hotels` | GET | Yes | List all hotels |
| `/api/hotels/{id}` | GET | Yes | Hotel details with amenities |
| `/api/room-types/{id}/availability` | POST | Yes | Check room availability & pricing |
| `/api/bookings` | POST | Yes | Create a booking |
| `/api/bookings/{id}` | GET | Yes | Retrieve booking details |
| `/api/bookings/{id}/cancel` | POST | Yes | Cancel a booking |
| `/api/amenities` | GET | Yes | List amenities by scope |

See OpenAPI specification at:
```
http://localhost:8080/v3/api-docs
```

---

## 🧱 Design Principles

### 1. **Modular Monolith Architecture**
Services are organized by domain (auth, catalog, booking, payment, notification) with clear module boundaries. This structure facilitates future decomposition into microservices without requiring immediate infrastructure overhead.

### 2. **Separation of Concerns**
- **Entity Layer** — JPA models representing database schema
- **Service Layer** — Business logic and orchestration
- **Controller Layer** — HTTP request handling and response formatting
- **Mapper Layer** — DTO transformation and data projection
- **Repository Layer** — Data access via Spring Data JPA

### 3. **Stateless REST Design**
All endpoints are stateless; client context is embedded in JWT tokens. This enables horizontal scaling, load balancing, and seamless service replication.

### 4. **Extensibility for Microservices**
Module packages align with potential microservice boundaries:
- `auth-service` → Authentication & Authorization
- `catalog-service` → Hotel, Room Type, Amenity Management
- `booking-service` → Reservation Orchestration
- `payment-service` → Order Processing
- `notification-service` → Async Notifications

### 5. **Exception Handling & Error Responses**
Centralized exception mapping via `@RestControllerAdvice` ensures consistent error responses with meaningful HTTP status codes.

### 6. **Testing Strategy**
- Unit tests validate service logic in isolation
- Integration tests verify workflows across module boundaries
- Test profile isolation prevents data leakage

---

## 👥 Authors

**Developed for Academic Submission**  
*University Project — Software Engineering Course*

Prepared as part of an academic software engineering project.

---

## 📄 Academic Note & License

This project is submitted as coursework and represents a comprehensive example of enterprise Java backend engineering. It demonstrates:

✓ RESTful API design patterns  
✓ JWT-based security implementation  
✓ Relational database modeling  
✓ Service-oriented architecture  
✓ Test-driven development practices  
✓ OpenAPI documentation standards  

**Project Status:** Stable and submission-ready for academic evaluation and future evolution.

**License:** Apache License 2.0 (or as specified by your institution)

---

## 🎯 Future Enhancements

- **Microservices Decomposition** — Modularize into separate payment, notification, and booking services
- **Event-Driven Architecture** — Apache Kafka for async inter-service communication
- **Real Payment Integration** — Stripe or PayPal for actual payment processing
- **Caching Layer** — Redis for availability and pricing optimization
- **API Rate Limiting & Monitoring** — Resilience4j, Prometheus metrics, and structured logging

---

**Last Updated:** April 7, 2026  
**Build Status:** ✅ Stable  
**Test Coverage:** Comprehensive
