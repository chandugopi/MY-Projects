# Cloud-Native Microservices Platform 🚀

A distributed banking system demonstrating microservices architecture with Spring Boot, Apache Kafka, Docker, and Kubernetes.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
└─────────────┬───────────────┬────────────────┬──────────────────┘
              │               │                │
    ┌─────────▼───────┐ ┌─────▼────────┐ ┌─────▼──────────┐
    │  User Service   │ │Account Service│ │Transaction Svc │
    │    (8081)       │ │   (8082)      │ │    (8083)      │
    └────────┬────────┘ └──────────────┘ └───────┬────────┘
             │                                    │
             ▼                                    ▼
    ┌────────────────────────────────────────────────────────┐
    │                   Apache Kafka                          │
    │     [user-events]     [transaction-events]              │
    └────────────────────────────┬───────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  Notification Service   │
                    │        (8084)           │
                    └─────────────────────────┘
```

## ✨ Features

- **User Service**: Registration, JWT authentication, role-based access
- **Account Service**: Bank account CRUD, deposits, withdrawals with pessimistic locking
- **Transaction Service**: Fund transfers, transaction history, Kafka event publishing
- **Notification Service**: Email/SMS notifications via Kafka consumer
- **Event-Driven**: Asynchronous communication using Apache Kafka
- **Containerized**: Docker images for each microservice
- **Cloud-Ready**: Kubernetes deployment manifests

## 🔧 Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Core language |
| Spring Boot 3.4 | Microservice framework |
| Spring Security | JWT Authentication |
| Spring Data JPA | Data access |
| Apache Kafka | Event streaming |
| Docker | Containerization |
| Kubernetes | Orchestration |
| H2/PostgreSQL | Database |

## 📁 Project Structure

```
cloud-microservices-platform/
├── common/                    # Shared DTOs, events, exceptions
├── user-service/              # User registration & authentication
├── account-service/           # Account management
├── transaction-service/       # Transaction processing
├── notification-service/      # Email/SMS notifications
├── docker/                    # Docker Compose files
└── k8s/                      # Kubernetes manifests
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Run Locally

```bash
# Build all modules
mvn clean package -DskipTests

# Start with Docker Compose
cd docker
docker-compose up -d

# Services available at:
# - User Service:        http://localhost:8081
# - Account Service:     http://localhost:8082
# - Transaction Service: http://localhost:8083
# - Notification Service: http://localhost:8084
```

### Run Individual Service

```bash
cd user-service
mvn spring-boot:run
```

## 📡 API Endpoints

### User Service (8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login & get JWT |

### Account Service (8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/accounts` | Create account |
| GET | `/api/accounts/{number}` | Get account |
| POST | `/api/accounts/{number}/deposit` | Deposit funds |
| POST | `/api/accounts/{number}/withdraw` | Withdraw funds |

### Transaction Service (8083)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/transfer` | Transfer funds |
| GET | `/api/transactions/{id}` | Get transaction |
| GET | `/api/transactions/user/{userId}` | Get user transactions |

## 🎯 Design Patterns & Concepts

| Pattern | Implementation |
|---------|----------------|
| **Microservices** | Independent, deployable services |
| **Event-Driven** | Kafka for async communication |
| **API Gateway** | Single entry point (can be added) |
| **JWT Auth** | Stateless authentication |
| **CQRS** | Separate read/write models |
| **Saga Pattern** | Distributed transactions |

## 🐳 Docker Commands

```bash
# Build all images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f user-service

# Stop all
docker-compose down
```

## ☸️ Kubernetes Deployment

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployments
kubectl get deployments

# Check services
kubectl get services
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Test specific module
cd user-service
mvn test
```

## 📋 Interview Discussion Points

1. **Why microservices over monolith?**
   - Independent scaling, deployment, and technology choices

2. **How do you handle distributed transactions?**
   - Saga pattern with compensating transactions

3. **Why Kafka for messaging?**
   - High throughput, durability, exactly-once semantics

4. **How do you ensure data consistency?**
   - Pessimistic locking, event sourcing, eventual consistency

5. **Container orchestration benefits?**
   - Auto-scaling, self-healing, rolling updates
