# Tiffin Platform — Microservices

## Project Structure

```
tiffin-platform/
│
├── pom.xml                     ← Parent POM (manages all versions)
├── docker-compose.yml          ← Spins up entire stack locally
├── scripts/
│   └── init-db.sql             ← Creates all service databases
│
├── common-lib/                 ← Shared library (NOT a service)
│   └── src/main/java/com/tiffin/common/
│       ├── dto/                ← Shared DTOs (add as needed)
│       ├── enums/              ← OrderStatus, PaymentStatus, UserRole
│       ├── exception/          ← TiffinException
│       ├── response/           ← ApiResponse<T> wrapper
│       └── utils/              ← (add helpers here)
│
├── api-gateway/                ← Port 8080  (Spring Cloud Gateway)
├── user-service/               ← Port 8081
├── provider-service/           ← Port 8082
├── menu-service/               ← Port 8083
├── order-service/              ← Port 8084
├── subscription-service/       ← Port 8085
├── payment-service/            ← Port 8086
├── admin-service/              ← Port 8087
└── notification-service/       ← No port (Kafka consumer only)
```

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `order.placed` | order-service | notification-service, provider-service |
| `order.status.updated` | order-service | notification-service |
| `payment.confirmed` | payment-service | order-service |
| `meal.ready` | provider-service | notification-service |
| `subscription.created` | subscription-service | notification-service |
| `user.registered` | user-service | notification-service |

## Service Port Map

| Service | Port | DB |
|---|---|---|
| api-gateway | 8080 | — |
| user-service | 8081 | tiffin_users |
| provider-service | 8082 | tiffin_providers |
| menu-service | 8083 | tiffin_menus |
| order-service | 8084 | tiffin_orders |
| subscription-service | 8085 | tiffin_subscriptions |
| payment-service | 8086 | tiffin_payments |
| admin-service | 8087 | tiffin_admin |
| notification-service | — | — |

## Running Locally

```bash
# 1. Build common-lib first (other services depend on it)
mvn install -pl common-lib

# 2. Start infrastructure (Kafka, Redis, Postgres)
docker-compose up -d zookeeper kafka redis postgres

# 3. Run any service from its directory
cd user-service && mvn spring-boot:run

# 4. Or build and run everything via Docker
docker-compose up -d
```

## Build Order Rule
Always run `mvn install -pl common-lib` before building any service.
common-lib must be in your local Maven repository before other modules can depend on it.
