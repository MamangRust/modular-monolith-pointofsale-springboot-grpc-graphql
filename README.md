# Distributed Modular Monolith — Point of Sale Platform (Java Spring Boot)

A production-grade, highly resilient, and fully observable **modular-monolith point of sale (POS) backend** built in **Java 21** using the **Spring Boot** framework (v3.5.6). Designed around domain-driven service boundaries following Clean Architecture and CQRS principles, it retains the operational and deployment simplicity of a single deployment unit while maintaining logical isolation typical of microservices.

Each retail and identity business domain — Users, Roles, Cashiers, Merchants, Categories, Products, Orders, Order Items, Transactions — lives in its own self-contained Maven module. These modules communicate synchronously via high-performance **gRPC** protocols and asynchronously using **Apache Kafka** event propagation, exposing a unified entry point through a **GraphQL Gateway (BFF)** powered by Spring GraphQL.

The platform is fortified with a **comprehensive observability suite** (Prometheus, Grafana, Loki, Jaeger, OpenTelemetry), robust connection pooling via **PgBouncer**, **distributed Redis Cluster caching** with custom telemetry for each service, and Kubernetes configurations ready for production auto-scaling.

---

## Key Features

| Domain             | Capabilities                                                                                                                                                                                              |
| :----------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Auth & Users**   | Secure registration, multi-factor login, stateless JWT access/refresh token lifecycle, password reset workflows, OTP email verification, and the `me` profile GraphQL query.                                 |
| **Roles & RBAC**   | Custom permission configuration, granular access control matrices, and sub-second permission evaluation cached via Redis.                                                                                 |
| **Merchants**      | Fully featured merchant onboarding, profile details management, business data registration, and merchant performance/transaction reports with full data restoration capabilities (soft delete & restore). |
| **Cashiers**       | Staff management per merchant, cashier activity tracking, sales performance analysis, and daily/monthly sales volume reporting.                                                                           |
| **Categories**     | Product taxonomy categorizations with soft-delete capabilities and quick search filters.                                                                                                                  |
| **Products**       | Inventory management, count in stock tracking, brand details, price structures, and multi-dimensional search filters (merchant, price range, brand, category).                                            |
| **Orders & Items** | Ledger checkout transactions, multi-item baskets with product price lookups, real-time total updates, monthly/yearly total revenue analytics, and sold-out product tracking.                              |
| **Transactions**   | Central financial audit register, global search filters, status tracking, monthly/yearly volume reports, and merchant/cashier sales breakdown.                                                            |
| **Email Worker**   | Kafka-driven asynchronous worker dispatching critical notification emails (OTPs, login alerts, merchant onboarding notices, and order receipts/invoices) via SMTP.                                        |
| **Observability**  | Multi-dimensional metrics (Prometheus + Grafana), log aggregation (Loki + Logback), end-to-end distributed tracing (Jaeger + OpenTelemetry), and resource monitors (Node, Kafka, Postgres Exporters).     |
| **Deployment**     | Local orchestration using Docker Compose (featuring a 6-node Redis Cluster and PgBouncer), and auto-scaling Kubernetes manifests configured with Horizontal Pod Autoscalers (HPA).                        |

---

## Architecture Overview

The platform implements a **Distributed Modular Monolith** architecture. Each business service is logical, decoupled, and self-contained inside its own Maven submodule, possessing its own independent gRPC boundary. A **Spring Boot GraphQL Gateway** acts as the unified edge router (Backend-for-Frontend), transforming client **GraphQL queries and mutations** into fast gRPC downstream communications via Spring Boot gRPC clients.

### Core Architecture Principles

- **Domain-Driven Boundary Isolation**: Every service owns its database access, caching layers, and service logic, strictly forbidding cross-boundary database sharing.
- **Clean Architecture & CQRS**: Separation of concerns using `Handler (gRPC) → Service (Command/Query) → Repository (Command/Query)` layers ensures business logic remains clean, performant, and framework-agnostic.
- **Servlet execution**: Powered entirely by the Spring Boot servlet (Tomcat) execution model, keeping the stack familiar and operationally simple while gRPC handles synchronous inter-service calls.
- **GraphQL BFF Edge**: The gateway exposes a single `/graphql` endpoint (with GraphiQL IDE at `/graphiql`) whose `@QueryMapping` / `@MutationMapping` resolvers translate GraphQL operations into gRPC calls, giving clients one typed schema instead of dozens of REST routes.
- **PgBouncer Pooling**: Employs connection pooling to avoid PostgreSQL socket exhaustion across the multiple concurrent modular services.
- **Event-Driven Resilience**: Apache Kafka decouples transaction events, ensuring side effects like email billing remain completely non-blocking.
- **OTel Telemetry Integration**: Standardized OpenTelemetry middleware injects trace IDs across gRPC boundaries, allowing seamless trace propagation from the client GraphQL gateway down to postgres operations.

```mermaid
graph TB
    classDef client fill:#0f172a,stroke:#38bdf8,color:#e0f2fe,stroke-width:2px,font-weight:bold
    classDef gateway fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2px,font-weight:bold
    classDef domain fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:1.5px
    classDef infra fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef obs fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px
    classDef event fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:1.5px

    Client["Client Applications<br/>(Web / Mobile / API)"]:::client

    subgraph APIGateway["API Gateway — NGINX + Spring Boot GraphQL Gateway"]
        direction LR
        GQL["GraphQL Gateway (BFF)<br/>/graphql · GraphiQL /graphiql<br/>Port :8080"]:::gateway
        AuthMW["JWT Auth & Role<br/>Middleware"]:::gateway
    end

    Client -->|"GraphQL over HTTP (query/mutation)"| APIGateway

    subgraph BusinessServices["Business Domain Services (Java Spring Boot)"]
        direction TB

        subgraph IdentityDomain["Identity & Access"]
            AUTH["Auth Service<br/>JWT & BCrypt Server"]:::domain
            USER["User Service<br/>Profile Management"]:::domain
            ROLE["Role Service<br/>RBAC & Permissions"]:::domain
        end

        subgraph MerchantDomain["Merchant Management"]
            MERCH["Merchant Service<br/>Onboarding & Profiling"]:::domain
        end

        subgraph RetailDomain["Retail & Inventory Suite"]
            CASHIER["Cashier Service<br/>Staff & Sales Tracker"]:::domain
            CATEGORY["Category Service<br/>Product Taxonomy"]:::domain
            PRODUCT["Product Service<br/>Catalog & Inventory"]:::domain
        end

        subgraph OrderDomain["Checkout & Transactions"]
            ORDER["Order Service<br/>Checkout & Sales Ledger"]:::domain
            ORDER_ITEM["OrderItem Service<br/>Basket Details"]:::domain
            TXN["Transaction Service<br/>Central Audit Register"]:::domain
        end
    end

    GQL -->|"Spring Boot gRPC Client"| AUTH
    GQL -->|"Spring Boot gRPC Client"| USER
    GQL -->|"Spring Boot gRPC Client"| ROLE
    GQL -->|"Spring Boot gRPC Client"| MERCH
    GQL -->|"Spring Boot gRPC Client"| CASHIER
    GQL -->|"Spring Boot gRPC Client"| CATEGORY
    GQL -->|"Spring Boot gRPC Client"| PRODUCT
    GQL -->|"Spring Boot gRPC Client"| ORDER
    GQL -->|"Spring Boot gRPC Client"| ORDER_ITEM
    GQL -->|"Spring Boot gRPC Client"| TXN

    subgraph Infrastructure["Infrastructure Layer"]
        direction LR
        PGBOUNCER["PgBouncer<br/>Connection Pooler :6432"]:::infra
        PG[("PostgreSQL<br/>POINT_OF_SALE DB")]:::infra
        REDIS[("Redis Cluster<br/>6-Node Distributed Cache")]:::infra
        KAFKA[("Kafka Broker<br/>Event Bus")]:::infra
    end

    AUTH -->|"JDBC / JPA"| PGBOUNCER
    USER -->|"JDBC / JPA"| PGBOUNCER
    ROLE -->|"JDBC / JPA"| PGBOUNCER
    MERCH -->|"JDBC / JPA"| PGBOUNCER
    CASHIER -->|"JDBC / JPA"| PGBOUNCER
    CATEGORY -->|"JDBC / JPA"| PGBOUNCER
    PRODUCT -->|"JDBC / JPA"| PGBOUNCER
    ORDER -->|"JDBC / JPA"| PGBOUNCER
    ORDER_ITEM -->|"JDBC / JPA"| PGBOUNCER
    TXN -->|"JDBC / JPA"| PGBOUNCER

    PGBOUNCER --> PG

    AUTH -->|"Spring Data Redis client"| REDIS
    USER -->|"Spring Data Redis client"| REDIS
    ROLE -->|"Spring Data Redis client"| REDIS
    CASHIER -->|"Spring Data Redis client"| REDIS
    PRODUCT -->|"Spring Data Redis client"| REDIS
    GQL -->|"Spring Data Redis client"| REDIS

    subgraph EventConsumers["Event-Driven Consumers"]
        EMAIL["Email Service<br/>SMTP Notification Worker"]:::event
    end

    KAFKA -->|"Consume Events"| EMAIL

    subgraph Observability["Observability Stack"]
        direction LR
        PROM["Prometheus<br/>Metrics Engine"]:::obs
        LOKI["Loki<br/>Log Aggregator"]:::obs
        JAEGER["Jaeger<br/>Distributed Traces"]:::obs
        GRAFANA["Grafana<br/>Unified Dashboards"]:::obs
        OTEL["OTel Collector<br/>Telemetry Pipeline"]:::obs
        PROMTAIL["Promtail<br/>Log Shipper"]:::obs
        NODEX["Node Exporter<br/>System Metrics"]:::obs
        KAFKAX["Kafka Exporter<br/>Broker Metrics"]:::obs
        PGX["Postgres Exporter<br/>DB Performance"]:::obs
    end

    AUTH -->|gRPC| USER
    AUTH -->|gRPC| ROLE
    MERCH -->|gRPC| USER
    CASHIER -->|gRPC| USER
    ORDER -->|gRPC| PRODUCT
    ORDER -->|gRPC| TXN

    AUTH -.->|"Publish Verification Event"| KAFKA
    ORDER -.->|"Publish Order Event"| KAFKA

    AUTH -.->|"/metrics"| PROM
    USER -.->|"/metrics"| PROM
    ROLE -.->|"/metrics"| PROM
    MERCH -.->|"/metrics"| PROM
    CASHIER -.->|"/metrics"| PROM
    CATEGORY -.->|"/metrics"| PROM
    PRODUCT -.->|"/metrics"| PROM
    ORDER -.->|"/metrics"| PROM
    TXN -.->|"/metrics"| PROM
    GQL -.->|"/metrics"| PROM

    AUTH -.->|"OTLP Spans"| OTEL
    USER -.->|"OTLP Spans"| OTEL
    ROLE -.->|"OTLP Spans"| OTEL
    MERCH -.->|"OTLP Spans"| OTEL
    CASHIER -.->|"OTLP Spans"| OTEL
    CATEGORY -.->|"OTLP Spans"| OTEL
    PRODUCT -.->|"OTLP Spans"| OTEL
    ORDER -.->|"OTLP Spans"| OTEL
    TXN -.->|"OTLP Spans"| OTEL
    GQL -.->|"OTLP Spans"| OTEL

    OTEL -.-> JAEGER
    PROMTAIL -.-> LOKI
    NODEX -.-> PROM
    KAFKAX -.-> PROM
    PGX -.-> PROM
    PROM -.-> GRAFANA
    LOKI -.-> GRAFANA
    JAEGER -.-> GRAFANA
    KAFKA -.-> KAFKAX
    PG -.-> PGX
```

---

## Service Catalog

The modular architecture consists of **14 logical micro-applications** plus supporting database and migrations:

```mermaid
graph LR
    classDef svc fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1px,rx:8
    classDef gw fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2px,rx:8,font-weight:bold
    classDef support fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1px,rx:8

    subgraph Gateway
        API["API Gateway<br/>Spring Boot GraphQL Gateway"]:::gw
    end

    subgraph Identity["Identity & Access (3)"]
        A1["auth"]:::svc
        A2["user"]:::svc
        A3["role"]:::svc
    end

    subgraph Merchant["Merchant Suite (1)"]
        M1["merchant"]:::svc
    end

    subgraph Retail["Retail Suite (3)"]
        R1["cashier"]:::svc
        R2["category"]:::svc
        R3["product"]:::svc
    end

    subgraph Movements["Checkout Movements (3)"]
        T1["order"]:::svc
        T2["order_item"]:::svc
        T3["transaction"]:::svc
    end

    subgraph Support["Support Services (2)"]
        S1["email-service"]:::support
        S2["common"]:::support
    end

    API -->|"gRPC Client"| Identity
    API -->|"gRPC Client"| Merchant
    API -->|"gRPC Client"| Retail
    API -->|"gRPC Client"| Movements
```

---

## Internal Service Architecture

Every logical business service is mapped as a decoupled submodule following structured clean architecture rules.

```mermaid
graph TB
    classDef handler fill:#1e3a5f,stroke:#7dd3fc,color:#e0f2fe,stroke-width:1.5px
    classDef service fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1.5px
    classDef repo fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef infra fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px
    classDef shared fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:1.5px

    subgraph Service["Maven Module: <service-name>/"]
        direction TB

        subgraph SrcJava["src/main/java/com/sanedge/<service>/"]
            direction TB
            HANDLER["handler/<br/>gRPC Service Handlers"]:::handler
            SVC["service/ & service.impl/<br/>CQRS Business Logic"]:::service
            REPO["repository/<br/>JPA Repositories"]:::repo
            MODEL["entity/ / domain/<br/>Entities & Domain Models"]:::repo
        end

        HANDLER --> SVC
        SVC --> REPO
        REPO --> MODEL
    end

    subgraph SharedLibs["common/ — Shared Maven Module"]
        direction LR
        CONFIG["config/<br/>AppConfig / JwtConfig"]:::shared
        FLYWAY["config/FlywayConfig<br/>Migrations Runner"]:::shared
        REDIS_CFG["config/RedisConfig<br/>Client Pools"]:::shared
        REDIS_SVC["service/RedisService<br/>Cache Actions"]:::shared
        OBS["observability/<br/>TracingMetrics / TelemetryConfig"]:::shared
        PB["proto stubs / pb<br/>gRPC Proto Stubs"]:::shared
    end

    subgraph Infrastructure["External Infrastructure"]
        direction LR
        PGDB[("PostgreSQL")]:::infra
        RCLUSTER[("Redis Cluster")]:::infra
        KAFKA[("Kafka Brokers")]:::infra
    end

    HANDLER --> PB
    SVC --> REDIS_SVC
    SVC --> OBS
    REPO --> PGDB
    REDIS_SVC --> RCLUSTER
```

---

## GraphQL API

The gateway exposes **one schema** assembled from per-domain `.graphqls` files (`gateway/src/main/resources/graphql/*.graphqls`) — 11 files total, layered onto a root `type Query` / `type Mutation` declared in `schema.graphqls` via `extend type`.

| Endpoint | Purpose |
| :--- | :--- |
| `POST /graphql` | Single GraphQL endpoint (queries + mutations) |
| `GET /graphiql` | Interactive GraphiQL IDE (enabled in dev) |
| `GET /graphql/schema` | Printed SDL schema (`spring.graphql.schema.printer.enabled=true`) |

Config (`gateway/src/main/resources/application.properties`):

```properties
spring.graphql.path=/graphql
spring.graphql.graphiql.enabled=true
spring.graphql.graphiql.path=/graphiql
spring.graphql.schema.printer.enabled=true
spring.graphql.schema.locations=classpath:graphql/**
```

### Resolvers

Each domain has a Spring `@Controller` resolver (`gateway/.../resolver/`) with `@QueryMapping` / `@MutationMapping` methods that call the matching gRPC blocking stub:

| Resolver | Domain |
| :--- | :--- |
| `AuthResolver` | `me`, `healthchecker`, `register`, `login`, `refreshToken` |
| `UserResolver` / `RoleResolver` | user & role CRUD + pagination + soft-delete lifecycle |
| `MerchantResolver` / `CashierResolver` | merchant & cashier suite |
| `CategoryResolver` / `ProductResolver` | catalog |
| `OrderResolver` / `TransactionResolver` | checkout, orders & payments |

### Conventions

- **`ApiResponse` envelope** — mutations/queries return typed `*ApiResponse` objects (`{ status, message, data }`) mirroring the former REST envelope.
- **Pagination** — list queries accept a `PageInput { page, pageSize, search }` and return a `PageInfo { page, pageSize, totalItems, totalPages }` alongside `data`.
- **Soft-delete lifecycle** — most domains expose `active*` (deleted_at IS NULL), `trashed*` (deleted_at IS NOT NULL), plus `trash*` / `restore*` / `delete*Permanent` / `restoreAll*` / `deleteAll*Permanent` mutations.
- **`Long` scalar** — registered via `GraphQlConfig` (`ExtendedScalars.GraphQLLong`), declared in `common.graphqls`.
- **Auth** — `POST /graphql` and `/graphiql` are public matchers; the `AuthTokenFilter` reads the `Authorization: Bearer <jwt>` header and populates the `SecurityContext` (used e.g. by the `me` resolver).

### Example

```graphql
# Query — public health check
query { healthchecker }

# Mutation — login, then use access_token as Bearer for authenticated calls
mutation {
  login(input: { username: "user@example.com", password: "secret" }) {
    status
    message
    data { access_token refresh_token }
  }
}

# Authenticated query — current profile
query {
  me {
    status
    message
    data { id username firstname lastname email }
  }
}
```

---

## Data & Event Flow

### Synchronous Flow (GraphQL Resolver & Cache Read-Through)

All external client API requests go through the single GraphQL endpoint (`/graphql`) defined in the Spring Boot GraphQL Gateway. The gateway validates the JWT, routes the GraphQL query/mutation to its `@QueryMapping` / `@MutationMapping` resolver, connects with the correct downstream gRPC modular server, checks the Redis Cluster cache, and fetches PostgreSQL through PgBouncer if a cache miss occurs.

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant GW as API Gateway<br/>(Spring Boot GraphQL Gateway)
    participant SVC as Domain Service<br/>(gRPC Server)
    participant REDIS as Redis Cluster
    participant PGB as PgBouncer
    participant DB as PostgreSQL

    C->>GW: GraphQL query/mutation (POST /graphql)
    GW->>GW: JWT Authentication Check
    GW->>GW: Resolver dispatch (@QueryMapping/@MutationMapping)
    GW->>SVC: gRPC Call (Protobuf payload)
    SVC->>REDIS: Check Cache (Redis Cluster)
    alt Cache Hit
        REDIS-->>SVC: Return Cached Response
    else Cache Miss
        SVC->>PGB: Acquire Connection
        PGB->>DB: JPA / JDBC Execution
        DB-->>PGB: DB Result Set
        PGB-->>SVC: Rows Mapped
        SVC->>REDIS: Populate Cache for next read
    end
    SVC-->>GW: gRPC Response payload
    GW-->>C: GraphQL Response (JSON { data, errors })
```

### Asynchronous Flow (Kafka Event Pipeline)

The platform uses **Apache Kafka (KRaft mode)** as its event backbone for **email notifications** (8 domain-specific topics) dispatched through the `EmailService`. All producers use the **Spring Kafka** (`org.springframework.kafka.core.KafkaTemplate`) integrated through Spring Boot, with `acks=all`, idempotent producers, and SASL/TLS support. The **transactional outbox pattern** (`OutboxPublisher` in auth and transaction) guarantees at-least-once delivery even during broker outages.

#### Kafka Topics Registry

| # | Topic | Producer Service | Consumer Service | Consumer Group | Commit Mode | Purpose |
|---|---|---|---|---|---|---|
| 1 | `email-service-topic-auth-register` | auth (outbox) | email-service | `email-service-group` | Manual | Registration welcome email |
| 2 | `email-service-topic-auth-forgot-password` | auth (outbox) | email-service | `email-service-group` | Manual | Password reset OTP email |
| 3 | `email-service-topic-auth-verify-code-success` | auth | email-service | `email-service-group` | Manual | Email verification success |
| 4 | `email-service-topic-transaction-create` | transaction (outbox) | email-service | `email-service-group` | Manual | Transaction receipt/invoice email |
| 5 | `email-service-topic-merchant-create` | merchant | email-service | `email-service-group` | Manual | Merchant onboarding welcome |
| 6 | `email-service-topic-merchant-update-status` | merchant | email-service | `email-service-group` | Manual | Merchant status change notification |
| 7 | `email-service-topic-merchant-document-create` | merchant | email-service | `email-service-group` | Manual | Merchant document upload notification |
| 8 | `email-service-topic-merchant-document-update-status` | merchant | email-service | `email-service-group` | Manual | Merchant document status change |
| 9 | `email-service-topic-email-retry` | email-service | RetryProcessor | `email-service-group` | Manual | Bounded retry queue (max 3 attempts, exponential backoff) |
| 10 | `email-service-topic-email-dlq` | email-service | — | — | — | Dead-letter queue (terminal failure sink) |

> **Total: 10 Kafka topics** (8 email notification + 2 email infrastructure)

#### Kafka Producer Architecture

Every service that publishes to Kafka (auth, merchant, order, transaction) has its own `KafkaService` bean wrapping a `KafkaProducer<String, String>`. The common producer features:

- **Idempotent producer** (`acks=all`, `enable.idempotence=true`) for exactly-once semantics
- **W3C Traceparent injection** — `traceparent` header injected into every record for end-to-end distributed tracing
- **Chaos Engineering integration** — the chaos manager evaluates a `kafka` policy per topic and can simulate message drops, rejections, or artificial latency
- **Centralized config** — `KafkaConfig.producer()` in `common/` module ensures identical settings across all services

```java
// Standard producer lifecycle (auth, merchant, order, transaction)
@Inject Vertx vertx;
private volatile KafkaProducer<String, String> producer;

@PostConstruct
void init() {
    Map<String, String> config = KafkaConfig.producer(
        bootstrapServers, acks, idempotence, KafkaSecurity.fromEnv());
    producer = KafkaProducer.create(vertx, config);
}
```

#### Transactional Outbox Pattern

The **auth** and **transaction** services implement the **transactional outbox pattern** for reliable event publishing. Events are first written to an outbox table (PostgreSQL) within the same transaction as the business operation, then a scheduled poller (`@Scheduled(fixedDelay = 5000)`) publishes them to Kafka with at-least-once guarantees:

```mermaid
sequenceDiagram
    autonumber
    participant SVC as AuthService / TransactionService
    participant DB as PostgreSQL<br/>(outbox table)
    participant POLLER as OutboxPublisher<br/>(5s polling)
    participant K as Kafka Broker
    participant EMAIL as EmailService
    participant SMTP as SMTP Server

    SVC->>DB: Business operation + INSERT INTO outbox (atomic)
    DB-->>SVC: ✅ Transaction committed
    POLLER->>DB: SELECT PENDING rows + claim
    POLLER->>K: Publish email event (with retries)
    K-->>EMAIL: Consume (manual commit)
    EMAIL->>EMAIL: Idempotency guard (Redis SET NX PX)
    alt CLAIMED
        EMAIL->>SMTP: JavaMailSender.send()
        SMTP-->>EMAIL: ✅ Sent
        EMAIL->>K: Commit offset
    else DUPLICATE
        EMAIL->>K: Skip + Commit offset
    end
```

Key outbox guarantees:
- **Atomic writes** — business operation and outbox insert share the same DB transaction
- **Fixed-interval polling** — `@Scheduled(fixedDelay = 5000)` polls, claims, and publishes sequentially
- **Max attempts** — events exceeding `outbox.publisher.max-attempts` (default: 5) are marked FAILED (dead letter)
- **Backlog metrics** — `outbox_published_total` and `outbox_failed_total` counters exposed via OTel

#### Email Notification Flow

The email worker (`EmailService.java`) is a single consumer subscribing to all 8 notification topics. Key delivery guarantees:

- **Manual commit** — offsets are committed only after a terminal outcome (sent, retry published, or DLQ published)
- **Per-partition ordering** — `partitionTails` map ensures records within a partition are processed sequentially
- **Idempotency** — Redis-backed `EmailDedupGuard` claims `email:idempotency:<event_id>` atomically with a lease (fail-open if Redis is unavailable)
- **Bounded retry** — failed SMTP sends are re-published to `email-service-topic-email-retry` with exponential backoff (30s base, 5-minute cap), up to `email.retry.max-attempts` (default: 3)
- **Dead-letter queue** — exhausted retries move to `email-service-topic-email-dlq` (shared across all topics)
- **Per-partition consumer lag** — exposed as OTel gauge `kafka_consumer_lag{group, partition}`
- **Distributed tracing** — `traceparent` W3C header extracted from record headers for end-to-end span propagation

```mermaid
sequenceDiagram
    autonumber
    participant SVC as Domain Service<br/>(via outbox or direct)
    participant K as Kafka Broker
    participant EMAIL as EmailService<br/>(email-service-group)
    participant GUARD as Redis IdempotencyGuard
    participant RETRY as RetryProcessor
    participant SMTP as SMTP Server
    participant DLQ as DLQ Topic

    SVC->>K: Publish {event_id, schema_version=1, email, subject, body}
    K-->>EMAIL: Consume (per-partition ordered)
    EMAIL->>GUARD: claim(event_id) — SET NX PX
    alt CLAIMED
        EMAIL->>SMTP: JavaMailSender.send()
        SMTP-->>EMAIL: ✅ Sent
        EMAIL->>GUARD: markSent(event_id) — SENT TTL 24h
        EMAIL->>K: Commit offset
    else DUPLICATE
        EMAIL->>K: Skip + Commit offset
    else BUSY (lease held)
        EMAIL->>K: Do NOT commit (retry after backoff)
    end
    alt SMTP failure
        EMAIL->>K: Publish to email-service-topic-email-retry
        K-->>RETRY: Consume with backoff
        RETRY->>EMAIL: Re-consume (attempt N+1)
    end
    alt max_retries exceeded
        EMAIL->>K: Publish to email-service-topic-email-dlq
        EMAIL->>K: Commit offset (terminal)
    end
```

#### Kafka Security & Production Configuration

The `KafkaSecurity` record (common module) centralizes SASL/TLS configuration. Domain services use `KafkaConfig.producer()` / `KafkaConfig.consumer()` which auto-applies security settings from `KAFKA_*` env vars:

| Config | Environment Variable | Default | Description |
|---|---|---|---|
| `security.protocol` | `KAFKA_SECURITY_PROTOCOL` | `PLAINTEXT` | Protocol (`PLAINTEXT` / `SASL_SSL`) |
| `sasl.mechanism` | `KAFKA_SASL_MECHANISM` | `SCRAM-SHA-256` | SASL mechanism |
| `sasl.jaas.config` | `KAFKA_SASL_JAAS_CONFIG` | — | JAAS login module config |
| `ssl.truststore.location` | `KAFKA_SSL_TRUSTSTORE_LOCATION` | — | JKS truststore path |
| `ssl.keystore.location` | `KAFKA_SSL_KEYSTORE_LOCATION` | — | JKS keystore path |
| `enable.idempotence` | `KAFKA_IDEMPOTENCE` | `true` | Idempotent producer |
| `acks` | `KAFKA_ACKS` | `all` | Acknowledgement level |

#### Kafka Metrics (OTel)

| Metric | Type | Description |
|---|---|---|
| `email_sent_total` | Counter | Emails successfully sent via SMTP |
| `email_failed_total` | Counter | Email send attempts that failed |
| `email_retried_total` | Counter | Emails routed to the retry topic |
| `email_dlq_total` | Counter | Emails moved to DLQ |
| `email_duplicate_total` | Counter | Duplicate email events skipped |
| `email_invalid_event_total` | Counter | Events rejected for invalid envelope |
| `email_processing_duration_seconds` | Histogram | Email record processing duration |
| `kafka_consumer_lag{group, partition}` | Gauge | Per-partition consumer lag (email group) |
| `outbox_published_total{service}` | Counter | Events published from outbox (auth, transaction) |
| `outbox_failed_total{service}` | Counter | Outbox events that reached max attempts |

---

## Design Decisions & Known Limitations

Keputusan desain yang disengaja (bukan bug) — didokumentasikan agar tim tidak
"memperbaiki" perilaku berikut tanpa sadar:

| ID | Keputusan | Perilaku | Alasan |
|---|---|---|---|
| OT-3 | Status transaksi **tidak terkunci** | `updateTransaction` dapat mengubah `payment_status` kapan pun (dari pending → success/failed dan sebaliknya) tanpa state machine transisi | Audit status tidak dianggap kritikal untuk POS skala ini; mengubahnya menjadi state machine menambah kompleksitas tanpa kebutuhan bisnis eksplisit |
| OT-4 | Edge-case stok saat **trash item eksplisit** setelah trash order | Bila item di-trash eksplisit setelah order di-trash, stok item tersebut **tidak di-decrement** saat order di-restore (hanya item yang masih aktif yang ikut restore) | Perilaku disengaja: restore hanya memproses item aktif; item trash eksplisit adalah keputusan terpisah dari siklus hidup order. Konsekuensi: stok bisa bergeser dari "aktif = stok terpakai" pada skenario ini |
| OT-2 | `amount` transaksi **dihitung ulang server-side** | Nilai `amount` dari client tidak dipercaya; dihitung ulang dari order items + PPN 11% (`totalAmountWithTax`). Klaim yang kurang → `"Insufficient payment amount"` | Mencegah manipulasi nilai transaksi oleh client |

**Catatan Fase 11–15 (2026-08-14):** lihat `SUPER_PLANNING_MASTER.md` untuk
checklist lengkap — transactional outbox + retry/DLQ (F11), idempotency key
transaksi (F12), chaos & tracing Kafka (F13), SASL/TLS + acks=all + idempotent
producer (F14), serta order stats by-id & OTP (F15).

---

## Observability Architecture

```mermaid
graph TB
    classDef service fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:1.5px
    classDef collector fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef storage fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px
    classDef viz fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:2px,font-weight:bold

    subgraph Sources["Telemetry Sources"]
        direction TB
        SVCS["All Business Services<br/>(11 services)"]:::service
        KAFKA_SRC["Kafka Broker"]:::service
        NODES["Host / Node"]:::service
        DB_SRC["PostgreSQL Engine"]:::service
    end

    subgraph Collectors["Collection Layer"]
        direction TB
        PROM["Prometheus<br/>Scrapes /metrics"]:::collector
        PROMTAIL["Promtail<br/>Ships container logs"]:::collector
        OTEL["OTel Collector<br/>Receives OTLP spans"]:::collector
        NODEX["Node Exporter<br/>CPU / Memory / Disk / Net"]:::collector
        KAFKAX["Kafka Exporter<br/>Topic lag / Broker health"]:::collector
        PGX["Postgres Exporter<br/>PgBouncer & Query performance"]:::collector
    end

    subgraph Storage["Storage Layer"]
        direction TB
        PROM_TSDB["Prometheus TSDB<br/>(Metrics)"]:::storage
        LOKI_STORE["Loki<br/>(Log Index + Chunks)"]:::storage
        JAEGER_STORE["Jaeger<br/>(Trace Storage)"]:::storage
    end

    subgraph Visualization["Visualization & Alerting"]
        GRAFANA["Grafana<br/>Unified Dashboards"]:::viz
        ALERTMGR["Alertmanager<br/>Alert Routing"]:::viz
    end

    SVCS -->|"/metrics"| PROM
    SVCS -->|"OTLP gRPC"| OTEL
    SVCS -->|"stdout/stderr"| PROMTAIL
    NODES --> NODEX
    KAFKA_SRC --> KAFKAX
    DB_SRC --> PGX

    NODEX --> PROM
    KAFKAX --> PROM
    PGX --> PROM
    PROM --> PROM_TSDB
    PROMTAIL --> LOKI_STORE
    OTEL --> JAEGER_STORE

    PROM_TSDB --> GRAFANA
    LOKI_STORE --> GRAFANA
    JAEGER_STORE --> GRAFANA
    PROM_TSDB --> ALERTMGR
```

| Pillar       | Tool                   | Purpose                                                                                         |
| :----------- | :--------------------- | :---------------------------------------------------------------------------------------------- |
| **Metrics**  | Prometheus + Grafana   | Core metrics tracking (CPU, memory, request error rates, gRPC latencies, DB connection states). |
| **Logging**  | Loki + Logback         | Centralized structured JSON logger for indexing logs by service, queryable via LogQL.           |
| **Tracing**  | OpenTelemetry + Jaeger | Distributed system tracing across API gateway and internal gRPC services.                       |
| **Alerting** | Alertmanager           | Automated notification system triggered during latency hikes or service disconnects.            |

## Chaos Engineering Platform

The payment gateway features a built-in **Chaos Engineering engine** to continuously test system resilience under failure conditions (database spikes, slow endpoints, CPU stress, and memory leaks).

### How It Works

The chaos engine is managed by [ChaosManager.java](./common/src/main/java/com/sanedge/common/chaos/ChaosManager.java) which dynamically watches the configuration file [chaos.yaml](./chaos.yaml) for modifications:

- **Dynamic Hot-Reloading**: Every 5 seconds, the engine checks `chaos.yaml` for changes. Adjusting values or toggling policies will update the running system instantly without requiring a service restart.

### Injection Mechanisms

1. **HTTP Routing Chaos** ([ChaosHttpMiddleware.java](./common/src/main/java/com/sanedge/common/chaos/ChaosHttpMiddleware.java)): Intercepts API router entry points to inject specified latency hikes or HTTP errors (e.g., status code 429 - rate limits).
2. **Database SQL Chaos** ([ChaosSqlProxy.java](./common/src/main/java/com/sanedge/common/chaos/ChaosSqlProxy.java)): Wraps database clients in a dynamic proxy, injecting database transaction latency or simulating sudden lock wait timeouts/deadlocks when queries hit matching tables.
3. **Resource Stress Chaos** ([ChaosResourceSabotage.java](./common/src/main/java/com/sanedge/common/chaos/ChaosResourceSabotage.java)): Spawns CPU/memory pressure routines to simulate container hardware throttling or memory exhaustion.

---

## Deployment Architectures

### Docker Compose (Local Development)

The Docker Compose configuration provisions a 6-node Redis Cluster along with databases, event brokers, and service containers to replicate a microservices environment.

```mermaid
flowchart TB
    classDef gateway fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2px,font-weight:bold
    classDef core fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1.5px
    classDef infra fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef obs fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px
    classDef event fill:#431407,stroke:#fb923c,color:#fed7aa,stroke-width:1.5px

    subgraph DockerCompose["docker-compose.yml — Local Environment"]

        subgraph Gateway["API Gateway"]
            NGINX["NGINX Proxy :80"]:::gateway
            APIGW["API Gateway Container<br/>Spring Boot GraphQL Gateway :8080"]:::gateway
        end

        subgraph Services["Core Service Containers"]
            subgraph Identity["Identity & Access"]
                AUTH["auth-service"]:::core
                USER["user-service"]:::core
                ROLE["role-service"]:::core
            end

            subgraph MerchantSuite["Merchant Domain"]
                MERCH["merchant-service"]:::core
            end

            subgraph RetailSuite["Retail Domain"]
                CASHIER["cashier-service"]:::core
                CATEGORY["category-service"]:::core
                PRODUCT["product-service"]:::core
            end

            subgraph MovementsSuite["Checkout & Sales"]
                ORDER["order-service"]:::core
                ORDER_ITEM["order-item-service"]:::core
                TXN["transaction-service"]:::core
            end
        end

        subgraph Infra["Infrastructure Suite"]
            PG[("PostgreSQL :5432")]:::infra
            PGB[("PgBouncer :6432")]:::infra
            REDIS_CLUSTER[("Redis Cluster :6379-6384<br/>6 Nodes Enabled")]:::infra
            KAFKA[("Kafka Broker :9092")]:::infra
        end

        subgraph Obs["Observability Stack"]
            PROM["Prometheus :9090"]:::obs
            GRAFANA["Grafana :3000"]:::obs
            LOKI["Loki :3100"]:::obs
            JAEGER["Jaeger :16686"]:::obs
            OTEL["OTel Collector :4317"]:::obs
            NODEX["Node Exporter"]:::obs
            KAFKAX["Kafka Exporter"]:::obs
            PGX["Postgres Exporter"]:::obs
            PROMTAIL["Promtail Log Shipper"]:::obs
        end

        subgraph Events["Event Consumers"]
            EMAIL["Email Worker"]:::event
        end
    end

    NGINX --> APIGW

    APIGW -->|gRPC| AUTH
    APIGW -->|gRPC| USER
    APIGW -->|gRPC| ROLE
    APIGW -->|gRPC| MERCH
    APIGW -->|gRPC| CASHIER
    APIGW -->|gRPC| CATEGORY
    APIGW -->|gRPC| PRODUCT
    APIGW -->|gRPC| ORDER
    APIGW -->|gRPC| ORDER_ITEM
    APIGW -->|gRPC| TXN

    AUTH -->|SQL| PGB
    USER -->|SQL| PGB
    ROLE -->|SQL| PGB
    MERCH -->|SQL| PGB
    CASHIER -->|SQL| PGB
    CATEGORY -->|SQL| PGB
    PRODUCT -->|SQL| PGB
    ORDER -->|SQL| PGB
    ORDER_ITEM -->|SQL| PGB
    TXN -->|SQL| PGB

    PGB --> PG

    AUTH -->|Cache| REDIS_CLUSTER
    USER -->|Cache| REDIS_CLUSTER
    ROLE -->|Cache| REDIS_CLUSTER
    MERCH -->|Cache| REDIS_CLUSTER
    CASHIER -->|Cache| REDIS_CLUSTER
    PRODUCT -->|Cache| REDIS_CLUSTER
    APIGW --> REDIS_CLUSTER

    AUTH -->|gRPC| USER
    AUTH -->|gRPC| ROLE
    MERCH -->|gRPC| USER
    CASHIER -->|gRPC| USER
    ORDER -->|gRPC| PRODUCT
    ORDER -->|gRPC| TXN

    ORDER -->|Events| KAFKA

    KAFKA --> EMAIL

    AUTH -.->|"Metrics"| PROM
    USER -.->|"Metrics"| PROM
    ROLE -.->|"Metrics"| PROM
    MERCH -.->|"Metrics"| PROM
    CASHIER -.->|"Metrics"| PROM
    CATEGORY -.->|"Metrics"| PROM
    PRODUCT -.->|"Metrics"| PROM
    ORDER -.->|"Metrics"| PROM
    TXN -.->|"Metrics"| PROM
    APIGW -.->|"Metrics"| PROM

    AUTH -.->|"Traces"| OTEL
    USER -.->|"Traces"| OTEL
    ROLE -.->|"Traces"| OTEL
    MERCH -.->|"Traces"| OTEL
    CASHIER -.->|"Traces"| OTEL
    CATEGORY -.->|"Traces"| OTEL
    PRODUCT -.->|"Traces"| OTEL
    ORDER -.->|"Traces"| OTEL
    TXN -.->|"Traces"| OTEL
    APIGW -.->|"Traces"| OTEL

    OTEL -.-> JAEGER
    PROMTAIL -.-> LOKI
    PROM -.-> GRAFANA
    LOKI -.-> GRAFANA

    KAFKA -.-> KAFKAX
    PG -.-> PGX
    KAFKAX -.-> PROM
    PGX -.-> PROM
    NODEX -.-> PROM
```

---

### Kubernetes (Production Clustering)

The production-grade Kubernetes architecture is designed for high availability, fault tolerance, and seamless horizontal scaling. All manifests are defined inside the custom `point-of-sale` namespace, route edge traffic using NGINX pods acting as a LoadBalancer, and manage service scalability using individual HPAs.

```mermaid
flowchart TB
    classDef client fill:#0f172a,stroke:#38bdf8,color:#e0f2fe,stroke-width:2px,font-weight:bold
    classDef ingress fill:#0f172a,stroke:#06b6d4,color:#e0f7fa,stroke-width:2px,font-weight:bold
    classDef k8sSvc fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2px,font-weight:bold
    classDef pod fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1.5px
    classDef stateful fill:#172554,stroke:#60a5fa,color:#dbeafe,stroke-width:1.5px
    classDef hpa fill:#064e3b,stroke:#34d399,color:#ecfdf5,stroke-width:1px,stroke-dasharray: 5 5
    classDef obs fill:#052e16,stroke:#4ade80,color:#dcfce7,stroke-width:1.5px

    Client["Client Applications<br/>(HTTPS Requests)"]:::client

    subgraph K8sCluster["Kubernetes Cluster — Namespace: point-of-sale"]
        direction TB

        subgraph IngressLayer["Edge Reverse Proxy (NGINX)"]
            NGINX_SVC["nginx-service<br/>(LoadBalancer :80)"]:::k8sSvc
            NGINX_POD["nginx-pods"]:::pod
        end

        subgraph GatewayServices["GraphQL API Gateway (Scalable Deployment)"]
            APIGW_SVC["apigateway-service<br/>(ClusterIP :8080)"]:::k8sSvc
            APIGW_PODS["apigateway-pods"]:::pod
            APIGW_HPA["apigateway-hpa"]:::hpa
        end

        subgraph DomainServices["Internal gRPC Microservices"]
            direction TB

            subgraph IdentityZone["Identity Suite"]
                AUTH_POD["auth-pods"]:::pod
                USER_POD["user-pods"]:::pod
                ROLE_POD["role-pods"]:::pod
                AUTH_SVC["auth-service (gRPC)"]:::k8sSvc
                USER_SVC["user-service (gRPC)"]:::k8sSvc
                ROLE_SVC["role-service (gRPC)"]:::k8sSvc
            end

            subgraph MerchantZone["Merchant Suite"]
                MERCH_POD["merchant-pods"]:::pod
                MERCH_SVC["merchant-service (gRPC)"]:::k8sSvc
            end

            subgraph RetailZone["Retail & Taxonomy"]
                CASHIER_POD["cashier-pods"]:::pod
                CATEGORY_POD["category-pods"]:::pod
                PRODUCT_POD["product-pods"]:::pod
                CASHIER_SVC["cashier-service (gRPC)"]:::k8sSvc
                CATEGORY_SVC["category-service (gRPC)"]:::k8sSvc
                PRODUCT_SVC["product-service (gRPC)"]:::k8sSvc
            end

            subgraph MovementsZone["Ledgers & Orders"]
                ORDER_POD["order-pods"]:::pod
                TX_POD["transaction-pods"]:::pod
                ORDER_SVC["order-service (gRPC)"]:::k8sSvc
                TX_SVC["transaction-service (gRPC)"]:::k8sSvc
            end

            PodsHPA["Domain Services HPAs<br/>(auth, product, order, etc.)"]:::hpa
        end

        subgraph DataObservability["Infrastructure & Databases"]
            PGB_SVC["pgbouncer-service<br/>(ClusterIP :6432)"]:::k8sSvc
            PGB_POD["pgbouncer-pods"]:::pod

            PG_SVC["postgres-service<br/>(ClusterIP :5432)"]:::k8sSvc
            PG_POD["postgres-pods"]:::pod

            REDIS_SVC["redis-cluster-service<br/>(ClusterIP :6379)"]:::k8sSvc
            REDIS_SET[("redis-cluster StatefulSet<br/>(6-Node Shards)")]:::stateful

            KAFKA_SVC["kafka-service<br/>(ClusterIP :9092)"]:::k8sSvc
            KAFKA_POD["kafka-pods"]:::pod
        end

        subgraph BackgroundWorkers["Event Consumers"]
            EMAIL_SVC["email-service<br/>(ClusterIP)"]:::k8sSvc
            EMAIL_PODS["email-pods"]:::pod
            EMAIL_HPA["email-hpa"]:::hpa
        end

        subgraph K8sObs["Observability Namespace Suite"]
            PROM_SVC["prometheus-service<br/>(ClusterIP :9090)"]:::k8sSvc
            PROM_POD["prometheus-pod"]:::pod

            OTEL_SVC["otel-collector-service<br/>(ClusterIP :4317)"]:::k8sSvc
            OTEL_POD["otel-collector-pod"]:::pod

            LOKI_SVC["loki-service<br/>(ClusterIP :3100)"]:::k8sSvc
            LOKI_POD["loki-pod"]:::pod

            JAEGER_SVC["jaeger-service<br/>(ClusterIP :16686)"]:::k8sSvc
            JAEGER_POD["jaeger-pod"]:::pod

            GRAFANA_SVC["grafana-service<br/>(ClusterIP :3000)"]:::k8sSvc
            GRAFANA_POD["grafana-pod"]:::pod

            ALERTMGR_SVC["alertmanager-service<br/>(ClusterIP :9093)"]:::k8sSvc
            ALERTMGR_POD["alertmanager-pod"]:::pod

            PROMTAIL["promtail-daemonset"]:::pod

            KAFKAX_SVC["kafka-exporter-service"]:::k8sSvc
            KAFKAX_POD["kafka-exporter-pod"]:::pod

            NODEX_SVC["node-exporter-service"]:::k8sSvc
            NODEX_POD["node-exporter-daemonset"]:::pod
        end
    end

    Client -->|HTTPS :443| NGINX_SVC
    NGINX_SVC --> NGINX_POD
    NGINX_POD -->|Proxy Pass| APIGW_SVC
    APIGW_SVC --> APIGW_PODS
    APIGW_HPA -.->|Autoscales| APIGW_PODS

    APIGW_PODS -->|gRPC call| AUTH_SVC
    APIGW_PODS -->|gRPC call| USER_SVC
    APIGW_PODS -->|gRPC call| ROLE_SVC
    APIGW_PODS -->|gRPC call| MERCH_SVC
    APIGW_PODS -->|gRPC call| CASHIER_SVC
    APIGW_PODS -->|gRPC call| CATEGORY_SVC
    APIGW_PODS -->|gRPC call| PRODUCT_SVC
    APIGW_PODS -->|gRPC call| ORDER_SVC
    APIGW_PODS -->|gRPC call| TX_SVC

    AUTH_SVC --> AUTH_POD
    USER_SVC --> USER_POD
    ROLE_SVC --> ROLE_POD
    MERCH_SVC --> MERCH_POD
    CASHIER_SVC --> CASHIER_POD
    CATEGORY_SVC --> CATEGORY_POD
    PRODUCT_SVC --> PRODUCT_POD
    ORDER_SVC --> ORDER_POD
    TX_SVC --> TX_POD

    AUTH_POD -->|SQL| PGB_SVC
    USER_POD -->|SQL| PGB_SVC
    ROLE_POD -->|SQL| PGB_SVC
    MERCH_POD -->|SQL| PGB_SVC
    CASHIER_POD -->|SQL| PGB_SVC
    CATEGORY_POD -->|SQL| PGB_SVC
    PRODUCT_POD -->|SQL| PGB_SVC
    ORDER_POD -->|SQL| PGB_SVC
    TX_POD -->|SQL| PGB_SVC

    PGB_SVC --> PGB_POD
    PGB_POD -->|SQL| PG_SVC
    PG_SVC --> PG_POD

    AUTH_POD -->|Cache| REDIS_SVC
    USER_POD -->|Cache| REDIS_SVC
    ROLE_POD -->|Cache| REDIS_SVC
    MERCH_POD -->|Cache| REDIS_SVC
    CASHIER_POD -->|Cache| REDIS_SVC
    PRODUCT_POD -->|Cache| REDIS_SVC

    REDIS_SVC --> REDIS_SET

    AUTH_POD -->|gRPC| USER_SVC
    AUTH_POD -->|gRPC| ROLE_SVC
    MERCH_POD -->|gRPC| USER_SVC
    CASHIER_POD -->|gRPC| USER_SVC
    ORDER_POD -->|gRPC| PRODUCT_SVC
    ORDER_POD -->|gRPC| TX_SVC

    ORDER_POD -->|Events| KAFKA_SVC

    KAFKA_SVC --> KAFKA_POD
    KAFKA_POD -->|Message Stream| EMAIL_SVC
    EMAIL_SVC --> EMAIL_PODS

    EMAIL_HPA -.->|Autoscales| EMAIL_PODS

    PodsHPA -.->|Autoscales| AUTH_POD
    PodsHPA -.->|Autoscales| USER_POD
    PodsHPA -.->|Autoscales| ROLE_POD
    PodsHPA -.->|Autoscales| MERCH_POD
    PodsHPA -.->|Autoscales| CASHIER_POD
    PodsHPA -.->|Autoscales| CATEGORY_POD
    PodsHPA -.->|Autoscales| PRODUCT_POD
    PodsHPA -.->|Autoscales| ORDER_POD
    PodsHPA -.->|Autoscales| TX_POD

    AUTH_POD -.->|"Metrics"| PROM_SVC
    USER_POD -.->|"Metrics"| PROM_SVC
    ROLE_POD -.->|"Metrics"| PROM_SVC
    MERCH_POD -.->|"Metrics"| PROM_SVC
    CASHIER_POD -.->|"Metrics"| PROM_SVC
    CATEGORY_POD -.->|"Metrics"| PROM_SVC
    PRODUCT_POD -.->|"Metrics"| PROM_SVC
    ORDER_POD -.->|"Metrics"| PROM_SVC
    TX_POD -.->|"Metrics"| PROM_SVC
    APIGW_PODS -.->|"Metrics"| PROM_SVC

    AUTH_POD -.->|"Traces"| OTEL_SVC
    USER_POD -.->|"Traces"| OTEL_SVC
    ROLE_POD -.->|"Traces"| OTEL_SVC
    MERCH_POD -.->|"Traces"| OTEL_SVC
    CASHIER_POD -.->|"Traces"| OTEL_SVC
    CATEGORY_POD -.->|"Traces"| OTEL_SVC
    PRODUCT_POD -.->|"Traces"| OTEL_SVC
    ORDER_POD -.->|"Traces"| OTEL_SVC
    TX_POD -.->|"Traces"| OTEL_SVC
    APIGW_PODS -.->|"Traces"| OTEL_SVC

    PROM_SVC --> PROM_POD
    OTEL_SVC --> OTEL_POD
    LOKI_SVC --> LOKI_POD
    JAEGER_SVC --> JAEGER_POD
    GRAFANA_SVC --> GRAFANA_POD
    ALERTMGR_SVC --> ALERTMGR_POD

    OTEL_POD -.-> JAEGER_SVC
    PROMTAIL -.-> LOKI_SVC
    PROM_POD -.-> GRAFANA_SVC
    LOKI_POD -.-> GRAFANA_SVC
    PROM_POD -.-> ALERTMGR_SVC

    KAFKA_SVC -.-> KAFKAX_SVC
    KAFKAX_SVC --> KAFKAX_POD
    KAFKAX_POD -.-> PROM_SVC
    NODEX_SVC --> NODEX_POD
    NODEX_POD -.-> PROM_SVC
```

### ArgoCD App-of-Apps GitOps Architecture

The platform follows GitOps best practices using ArgoCD for declarative continuous deployments. Replicating the App-of-Apps design pattern, a root Application (`point-of-sale-root`) automatically manages and tracks the states of individual child Applications mapping to Kustomize bases.

Sync waves (`argocd.argoproj.io/sync-wave` annotations) are strictly defined to guarantee database migrations run and complete before domain applications start.

```mermaid
graph TD
    classDef root fill:#1e293b,stroke:#22d3ee,color:#cffafe,stroke-width:2.5px,font-weight:bold
    classDef proj fill:#0f172a,stroke:#38bdf8,color:#e0f2fe,stroke-width:2px
    classDef app fill:#1e1b4b,stroke:#a78bfa,color:#ede9fe,stroke-width:1.5px
    classDef wave fill:#1c1917,stroke:#f59e0b,color:#fef3c7,stroke-width:1.5px
    classDef base fill:#052e16,stroke:#34d399,color:#dcfce7,stroke-width:1.5px

    RootApp["point-of-sale-root<br/>(ArgoCD Root Application)"]:::root
    AppProj["pos<br/>(ArgoCD AppProject)"]:::proj

    %% Root to Project mapping
    RootApp --> AppProj

    %% Sync Waves Grouping
    subgraph Waves["ArgoCD Sync Waves Sequence"]
        direction TB

        subgraph Wave1["Wave 1: Core Foundation & Infrastructure"]
            W1_Common["common<br/>(deployments/kubernetes/base/common)"]:::base
            W1_Postgres["infra-postgres<br/>(deployments/kubernetes/base/postgres)"]:::base
            W1_Redis["infra-redis<br/>(deployments/kubernetes/base/redis)"]:::base
            W1_Kafka["infra-kafka<br/>(deployments/kubernetes/base/kafka)"]:::base
            W1_Obs["observability<br/>(deployments/kubernetes/base/observability)"]:::base
        end

        subgraph Wave2["Wave 2: Database Migration"]
            W2_Migrate["db-migration<br/>(deployments/kubernetes/base/db-migration)"]:::base
        end

        subgraph Wave3["Wave 3: Core Domain Services (gRPC/HTTP)"]
            W3_Auth["service-auth<br/>(deployments/kubernetes/base/auth)"]:::base
            W3_User["service-user<br/>(deployments/kubernetes/base/user)"]:::base
            W3_Role["service-role<br/>(deployments/kubernetes/base/role)"]:::base
            W3_Product["service-product<br/>(deployments/kubernetes/base/product)"]:::base
            W3_Category["service-category<br/>(deployments/kubernetes/base/category)"]:::base
            W3_Merchant["service-merchant<br/>(deployments/kubernetes/base/merchant)"]:::base
            W3_Order["service-order<br/>(deployments/kubernetes/base/order)"]:::base
            W3_Cashier["service-cashier<br/>(deployments/kubernetes/base/cashier)"]:::base
            W3_OrderItem["service-order-item<br/>(deployments/kubernetes/base/order_item)"]:::base
            W3_Email["service-email<br/>(deployments/kubernetes/base/email)"]:::base
        end

        subgraph Wave4["Wave 4: Financial Ledgers"]
            W4_Tx["service-transaction<br/>(deployments/kubernetes/base/transaction)"]:::base
        end

        subgraph Wave5["Wave 5: API Edge Gateway"]
            W5_Gate["apigateway<br/>(deployments/kubernetes/base/apigateway)"]:::base
        end

        subgraph Wave6["Wave 6: Ingress Control"]
            W6_Nginx["nginx<br/>(deployments/kubernetes/base/nginx)"]:::base
        end
    end

    AppProj --> Wave1
    Wave1 --> Wave2
    Wave2 --> Wave3
    Wave3 --> Wave4
    Wave4 --> Wave5
    Wave5 --> Wave6
```

### GitOps Application Registry

The directory layout under [deployments/gitops/argocd/](file:///home/hoover/Projects/java/SpringBoot/modular-grpc-graphql/pointofsale/deployments/gitops/argocd/) manages these deployments:

1. **Root Application**: [root-app.yaml](file:///home/hoover/Projects/java/SpringBoot/modular-grpc-graphql/pointofsale/deployments/gitops/argocd/root-app.yaml) bootstraps the GitOps sequence, pointing directly to the child applications namespace registry under `/apps`.
2. **Project Specification**: [project.yaml](file:///home/hoover/Projects/java/SpringBoot/modular-grpc-graphql/pointofsale/deployments/gitops/argocd/project.yaml) defines the target cluster destinations, namespace whitelists (e.g., `pos` and `pointofsale`), and cluster resources access controls.
3. **Application Definitions**: [apps/](file:///home/hoover/Projects/java/SpringBoot/modular-grpc-graphql/pointofsale/deployments/gitops/argocd/apps/) contains the declaration manifests for all 19 component applications.

---

## Technology Stack

| Category              | Selected Technologies        | Purpose                                                          |
| :-------------------- | :--------------------------- | :--------------------------------------------------------------- |
| **Language**          | Java 21 (Spring Boot v3.5.6) | Servlet-based (Tomcat) Java execution.                          |
| **API Edge Gateway**  | Spring GraphQL               | GraphQL BFF gateway (single `/graphql` endpoint + GraphiQL) proxying to gRPC. |
| **GraphQL API**       | Spring for GraphQL           | GraphQL endpoint at `/graphql` with GraphiQL IDE at `/graphiql`.  |
| **RPC Inter-service** | Spring Boot gRPC Client & Server | Blazing fast, contract-first synchronous gRPC communication.     |
| **Database**          | PostgreSQL v17               | Safe ACID ledger persistent storage system.                      |
| **Database Gateway**  | PgBouncer                    | Extreme-efficiency PostgreSQL socket connection pooler.          |
| **DB Migrations**     | Flyway                       | Incremental database schema version manager run on startup.      |
| **Caching Tier**      | Redis Cluster (6 Nodes)      | Resilient, distributed key-value cache layer.                    |
| **Messaging Stream**  | Apache Kafka                 | Asynchronous high-throughput messaging event bus (KRaft mode).   |
| **Token Manager**     | JWT                          | Secure stateless request authentication standard.                |
| **Observability**     | OpenTelemetry + Jaeger       | Vendor-neutral distributed telemetry pipeline and visualization. |
| **Docker Engine**     | Compose                      | Local environment virtualization orchestration.                  |
| **Orchestrator**      | Kubernetes                   | Production-scale auto-scaling pod clustering infrastructure.     |

---

## Getting Started

### Prerequisites

Ensure the following system packages are locally configured:

- [Git](https://git-scm.com/)
- [Java Development Kit (JDK 21+)](https://adoptium.net/)
- [Apache Maven](https://maven.apache.org/) (v3.9+)
- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- [Protobuf Compiler](https://grpc.io/docs/protoc-installation/) (optional — the build fetches `protoc` automatically)

### 1. Clone the Workspace

```sh
git clone https://github.com/MamangRust/example-pointofsale-springboot.git
cd example-pointofsale-springboot
```

### 2. Prepare Environment Configurations

Setup the system configurations from the provided placeholders:

```sh
# Copy the local docker settings (compose reads variable substitution from this file)
cp deployments/local/docker.env.example deployments/local/docker.env
cp deployments/local/docker.env.example deployments/local/.env
```

### 3. Build the Maven Project

Compile all submodules and build the executable JAR files:

```sh
mvn clean package -DskipTests
```

### 4A. Full Docker Compose (All-in-One)

Start the entire stack — infrastructure + Java services — as Docker containers.
Each service is built from its own Dockerfile (multi-stage: Maven build → JRE runtime)
via the `build:` section of the compose file, so no separate image-build script is needed:

```sh
# Build and start local infrastructure, telemetry containers, and application services
docker-compose -f deployments/local/docker-compose.yml up -d --build
```

Flyway database migrations run automatically on db-migration startup.

To verify the cluster services are up and healthy:

```sh
docker-compose -f deployments/local/docker-compose.yml ps
```

### 4B. Local Development (Recommended) ⚡

Run **infrastructure in Docker** and **Java services locally** for faster iteration:

```sh
# 1. Start only infrastructure (postgres, redis cluster, kafka, pgbouncer, observability)
cd deployments/local
docker compose up -d postgres pgbouncer redis-node-1 redis-node-2 redis-node-3 \
  redis-node-4 redis-node-5 redis-node-6 kafka jaeger otel-collector \
  prometheus grafana loki alertmanager

# 2. Run the database migration (Flyway) and wait for it to finish
docker compose up db-migration

# 3. Build the project (all modules)
cd ../..
mvn clean package -DskipTests

# 4. Start the services you need locally, e.g. the gRPC services + gateway
java -jar auth/target/auth-0.0.1-SNAPSHOT.jar &
java -jar user/target/user-0.0.1-SNAPSHOT.jar &
java -jar role/target/role-0.0.1-SNAPSHOT.jar &
java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar &
# ... repeat for merchant, cashier, category, product, order, order-item, transaction, email-service
```

This mode uses the same infrastructure containers but runs the Spring Boot fat
JARs as local JVMs on your machine, enabling faster feedback loops than a full
container rebuild.

The gateway listens on `http://localhost:8080` (override with `GATEWAY_PORT`);
check readiness at `http://localhost:8080/actuator/health`.

---

## Port Map Registry

### Infrastructure

| Component                       | Port Configuration / URL                                                        |
| :------------------------------ | :------------------------------------------------------------------------------ |
| **NGINX Reverse Proxy Edge**    | [http://localhost](http://localhost)                                            |
| **GraphQL Gateway Endpoint**    | [http://localhost:8080/graphql](http://localhost:8080/graphql)                  |
| **Grafana Dashboard Portal**    | [http://localhost:3000](http://localhost:3000) _(Credentials: `admin`/`admin`)_ |
| **Prometheus Telemetry**        | [http://localhost:9090](http://localhost:9090)                                  |
| **Jaeger Distributed Tracing**  | [http://localhost:16686](http://localhost:16686)                                |
| **PgBouncer Gateway Node**      | `localhost:6432`                                                                |
| **PostgreSQL Database Engine**  | `localhost:5432`                                                                |
| **Kafka Broker**               | `localhost:9092`                                                                |
| **Redis Cluster**              | `localhost:6379`–`localhost:6384` (6 nodes)                                      |

### Java Services (Local Development Mode)

Each gRPC business module binds a single Spring Boot port (`server.port` == `grpc.server.port`)
that serves the gRPC API. The gateway, DB migration job, and email worker are the only
modules with distinct HTTP surfaces.

| Service          | gRPC Port | HTTP / Actuator Port | Health Check URL                          |
| :--------------- | :-------- | :------------------- | :---------------------------------------- |
| **auth**         | 9012      | —                    | —                                         |
| **user**         | 9011      | —                    | —                                         |
| **role**         | 9006      | —                    | —                                         |
| **merchant**     | 9005      | —                    | —                                         |
| **category**     | 9015      | —                    | —                                         |
| **product**      | 9003      | —                    | —                                         |
| **cashier**      | 9014      | —                    | —                                         |
| **order**        | 9001      | —                    | —                                         |
| **order_item**   | 9016      | —                    | —                                         |
| **transaction**  | 9009      | —                    | —                                         |
| **email-service**| —         | 8098                 | `http://localhost:8098/actuator/health`   |
| **gateway**      | —         | 8080                 | `http://localhost:8080/actuator/health`   |

To stop the development system and clean up resources:

```sh
# Full Docker Compose
docker-compose -f deployments/local/docker-compose.yml down -v

# Local Java services only (stop JVMs launched with `java -jar ...`)
pkill -f 'target/.*\.jar'
```

---

## Maven & Shell Commands Reference

| Command                                                                    | Scope                                                                                                     |
| :------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------- |
| `mvn clean package -DskipTests`                                            | Compiles all submodules and generates executable JAR files (skipping tests for speed).                    |
| `mvn clean install`                                                        | Cleans target directories, runs tests, compiles all submodules, and generates package JARs.               |
| `mvn compile`                                                              | Compiles raw Java source files for all modules.                                                           |
| `mvn -pl <module> -am package -DskipTests`                                 | Builds a single module (and its reactor dependencies) without building the whole project.                 |
| `docker compose -f deployments/local/docker-compose.yml up -d --build`     | Builds all service images from their Dockerfiles and launches every container in background mode.         |
| `docker compose -f deployments/local/docker-compose.yml down`              | Stops compose containers, releasing standard networks.                                                    |
| `docker compose -f deployments/local/docker-compose.yml logs -f <service>` | Follows the realtime stdout logs of a specific service container.                                         |
| `java -jar <module>/target/<module>-0.0.1-SNAPSHOT.jar`                    | Runs a single Spring Boot service locally as a fat JAR (foreground).                                      |

---

## Workspace Directory Tree

```
springboot-point-of-sale/
├── pom.xml                         # Root Maven Parent POM
├── common/src/main/proto/          # Protobuf contracts (11 domains)
│   ├── auth.proto                  #   Identity tokens contracts
│   ├── cashier/                    #   Cashier and staff configurations
│   ├── category/                   #   Product category declarations
│   ├── common/                     #   Shared protobuf data types
│   ├── merchant/                   #   Merchant account declarations
│   ├── merchant_document/          #   Verification files specifications
│   ├── order/                      #   Order and payment details
│   ├── order_item/                 #   Detailed items list configurations
│   ├── product/                    #   Product CRUD and inventory properties
│   ├── role/                       #   Role mapping specifications
│   ├── transaction/                #   General audit register specifications
│   └── user/                       #   User CRUD data properties
├── common/                         # Shared Maven library Module
│   └── src/main/java/com/sanedge/common/
│       ├── config/                 #   AppConfig, JwtConfig, RedisConfig, FlywayConfig
│       ├── observability/          #   TracingMetrics config
│       ├── service/                #   RedisService utilities
│       └── pb/                     #   Compiled Java Protobuf gRPC stubs
├── gateway/                        # GraphQL Gateway (BFF): GraphQL → gRPC
├── auth/                           # Authentication engine service
├── user/                           # User profiles service (CQRS)
├── role/                           # RBAC authorization service
├── merchant/                       # Merchant onboarding & reports service
├── cashier/                        # Cashier & staff management service
├── category/                       # Category management service
├── product/                        # Product & inventory service
├── order/                          # Order and billing service
├── order_item/                     # Order items listing service
├── transaction/                    # Central transaction ledger audit service
├── email-service/                  # Asynchronous Kafka notifications service
├── deployments/
│   ├── local/                      #   Docker compose infrastructure files
│   └── kubernetes/                 #   Production K8s deployment manifests
├── observability/                  #   Telemetry pipelines configurations (Loki, OTEL, Alertmanager)
├── grafana/                        #   Pre-configured dashboard JSON files
├── nginx/                          #   Reverse-proxy NGINX rules
└── images/                         #   Architecture diagrams & dashboard screenshots
```

---

## Testing

Each business module ships its own test suite built on **JUnit 5** + **Spring Boot Test**.
Tests fall into two broad categories:

- **Handler / unit tests** — exercise the gRPC handlers and CQRS services in isolation
  (e.g. `UserCommandHandlerTest`, `CashierQueryHandlerTest`, `AuthHandlerTest`) using a
  `TestStreamObserver` to capture gRPC responses.
- **Integration tests** — `BaseIntegrationTest` subclasses boot the Spring context and
  validate repositories/services against a real (ephemeral H2 or containerized) database
  (e.g. `MerchantServiceIntegrationTest`, `TransactionStatsServiceIntegrationTest`).

### Running Tests

```sh
# Run every module's tests
mvn test

# Run a single module's tests
mvn -pl auth test
mvn -pl category test

# Skip tests during a packaging build
mvn clean package -DskipTests
```

---

## License

This project is open-sourced under the MIT License for educational and development purposes.

---

<p align="center">
  Built with Java 21, Spring Boot 3.5.6, gRPC, Apache Kafka, PostgreSQL, Redis Cluster, and Spring for GraphQL — a passion for high-performance modular monoliths.
</p>
