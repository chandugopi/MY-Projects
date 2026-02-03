# Enterprise Order & Workflow Management System

A Spring Boot enterprise application demonstrating **SOLID principles**, **design patterns**, and **Java best practices**.

**Course Context**: CSCI 6620 – Software Engineering

## 🎯 Features

- **Layered Architecture**: Controller → Service → Repository → Entity
- **Global Exception Handling**: Centralized error responses with @ControllerAdvice
- **DTO/Entity Separation**: Clean data transfer with validation
- **Workflow State Machine**: Order status transitions with validation
- **Audit Logging**: AOP-based method interception
- **Unit Testing**: 18 tests with JUnit 5 and Mockito

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      REST Controllers                        │
│                 (UserController, OrderController)            │
└───────────────────────────┬─────────────────────────────────┘
                            │ @Valid DTOs
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Service Layer                           │
│         (UserService, OrderService + StateMachine)           │
│              @Transactional | Business Logic                 │
└───────────────────────────┬─────────────────────────────────┘
                            │ Entities
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│            (Spring Data JPA Repositories)                    │
└───────────────────────────┬─────────────────────────────────┘
                            │ JPA/Hibernate
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database (H2/PostgreSQL)                  │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
src/main/java/com/portfolio/enterprise/
├── EnterpriseApplication.java     # Main entry point
├── config/
│   └── SecurityConfig.java        # Spring Security
├── controller/
│   ├── UserController.java        # User REST API
│   └── OrderController.java       # Order REST API
├── service/
│   ├── UserService.java           # User business logic
│   └── OrderService.java          # Order business logic
├── repository/
│   ├── UserRepository.java        # User data access
│   └── OrderRepository.java       # Order data access
├── entity/
│   ├── User.java                  # User JPA entity
│   ├── Order.java                 # Order JPA entity
│   └── OrderItem.java             # OrderItem JPA entity
├── dto/
│   ├── CreateUserRequest.java     # User creation DTO
│   ├── UserResponse.java          # User response DTO
│   ├── CreateOrderRequest.java    # Order creation DTO
│   └── ...                        # Other DTOs
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── InvalidOrderStateException.java
│   └── GlobalExceptionHandler.java  # @ControllerAdvice
├── workflow/
│   └── OrderStateMachine.java     # State pattern
└── audit/
    └── AuditLogAspect.java        # AOP logging
```

## 🔧 Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 21+ | Core language |
| Spring Boot 3.4 | Application framework |
| Spring Data JPA | Data access |
| Spring Security | Authentication |
| H2 / PostgreSQL | Database |
| Lombok 1.18.38 | Boilerplate reduction |
| MapStruct | DTO mapping |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |

## 🚀 Quick Start

```bash
cd enterprise-java-platform

# Build and run tests
mvn clean test

# Run the application
mvn spring-boot:run

# Access H2 Console
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:enterprisedb
```

## 📋 API Endpoints

### User API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create user |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users` | Get all users |
| GET | `/api/users/active` | Get active users |
| PATCH | `/api/users/{id}/deactivate` | Deactivate user |

### Order API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders/user/{userId}` | Get user's orders |
| PATCH | `/api/orders/{id}/confirm` | Confirm order |
| PATCH | `/api/orders/{id}/cancel` | Cancel order |

## 🔑 Design Patterns Implemented

| Pattern | Implementation |
|---------|----------------|
| **Factory** | Order creation based on type |
| **State** | OrderStateMachine for workflow |
| **Builder** | Entity builders with Lombok |
| **Repository** | Spring Data JPA |
| **DTO** | Request/Response objects |

## 📊 Order Workflow State Machine

```
PENDING ──► CONFIRMED ──► PROCESSING ──► SHIPPED ──► DELIVERED
    │           │              │
    └───────────┴──────────────┴──────► CANCELLED
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

**Test Coverage**: 19+ unit tests covering:
- Service layer (UserService, OrderService)
- State machine transitions
- Exception handling
- Validation

## 💡 Interview Questions This Project Answers

| Question | Covered In |
|----------|------------|
| How do you design scalable Java apps? | Layered architecture |
| What are SOLID principles? | Service/Repository separation |
| How do you handle exceptions? | GlobalExceptionHandler |
| How do you test services? | Mockito-based tests |
| How do you manage transactions? | @Transactional |
| What design patterns do you know? | State, Factory, Builder |

## 📝 License

MIT License
