# CLAUDE.md — E-Shopper Services

Backend services for a marketplace selling makeup, beauty, fragrance and luxury products in the India market.
This is a learning project that should still be built to production quality. The aim is to learn Spring Boot microservices and distributed architecture.

## Owner context
- Strong in Next.js, React, TypeScript, Node and databases. New to Spring in depth.
- When explaining Spring concepts, map them to Node/NestJS equivalents (e.g. `@Transactional` ≈ wrapping a handler in a DB transaction; Spring beans/DI ≈ NestJS providers).
- Prefer terse, direct answers with code. Explain *why* for architectural choices.

## Hard rules (do not violate)
1. **Exactly 3 Java services**: Catalog, User, Order. Never propose a new service. New features become a **module inside an existing service**.
2. **Database per service, schema per module.** No cross-service table access and no cross-service foreign keys. Reference other services' entities by UUID only.
3. Services expose **domain-shaped REST APIs**. No UI-shaped or screen-specific endpoints.
4. No API gateway, service mesh, Eureka or Config Server.
5. **No hand-rolled auth.** Keycloak owns credentials. Services validate JWTs only.
6. Never store raw bank or payout details. Store only the payment provider token.
7. No two-phase commit. Cross-service state changes use the **Saga pattern + transactional outbox**.

## Repo layout
```
/catalog-service     Java 21, Spring Boot 3.x, Spring Modulith
/user-service
/order-service
/infra
  docker-compose.yml postgres, redis (+ keycloak, kafka later)
/docs
  /adr               architecture decision records
```

## Services and modules

| Service | Port* | DB | Modules |
|---|---|---|---|
| catalog-service | 8081 | `catalog_db` | `catalog`, `inventory`, `search` |
| user-service | 8082 | `user_db` | `identity`, `customer`, `seller` |
| order-service | 8083 | `order_db` | `cart`, `checkout`, `order`, `payment`, `notification` |

\*Ports are a suggested default. Change them here if different.

- **Catalog:**
    - Owns brands, hierarchical categories, products, SKUs (shade name/hex, size ml, price), beauty attributes, limited editions and max quantity per customer.
    - `inventory` module: stock per SKU per warehouse, batch + expiry, FEFO picking, reservations with TTL.
    - `search` module: Postgres full-text search.
    - Redis cache-aside for product reads.
    - Consumes `SellerStatusChanged` to control product visibility.
- **User:**
    - `users.id` = Keycloak `sub`. Email is a cached copy, never a key.
    - User rows are created by JIT provisioning on first login.
    - Customer data: profile, addresses (one default), beauty profile, marketing consent, anonymization on account deletion.
    - Seller data: legal/display name, GSTIN, PAN, KYC status, onboarding status, payout token. Ownership is `sellers.owner_user_id`.
    - Publishes seller status events.
- **Order:**
    - Cart lives in Redis. Guest carts use an anonymous ID and are merged into the account on login.
    - Checkout is the **saga orchestrator**.
    - Orders store **snapshots** of the address and prices, and follow a state machine.
    - Payments use Razorpay in test mode, with idempotent placement and webhook handling. Order and payment state change in one local transaction.
    - Notifications are sent on order events.
    - Validate alcohol-based fragrance shipping restrictions at checkout.

## Java conventions
- Java 21, Spring Boot 3.x, **Spring Modulith**.
- Each top-level package under the application package is a module. Put non-public code in `<module>.internal`. Other modules may use only a module's top-level API.
  ```
  com.eshopper.catalog
    ├── catalog/          (public API: services, DTOs, events)
    │   └── internal/     (entities, repositories, impl)
    ├── inventory/
    └── search/
  ```
- Add a `ModularityTests` class that calls `ApplicationModules.of(App.class).verify()`. It must stay green.
- Modules inside a service talk through **application events** (Spring Modulith event publication registry), not by calling each other's internals.
- **IDs:** UUIDv7, generated in the application, stored as native Postgres `UUID`. A DB default exists only as a safety net.
- Entities extend a shared `BaseEntity`: field-initialized v7 `id` + `@Version` for optimistic locking.
- Use static factory methods only for aggregates with creation rules or events (`Order`, `Seller`). Other entities use plain constructors.
- Manage schemas with Flyway migrations, one schema per module. Never edit an applied migration.
- Write integration tests with Testcontainers (Postgres, Redis). Do not use H2.
- Use Resilience4j on outbound REST calls between services (timeouts, retry, circuit breaker).

## Security
- Services are OAuth2 resource servers and validate Keycloak JWTs.
- Coarse roles (customer, seller, admin) come from the JWT. Fine-grained seller permissions live in the User Service DB, not in the JWT.

## Cross-service communication
- Use synchronous REST only for queries and saga steps, e.g. Order → Catalog to reserve/confirm/release stock, and Order → User to fetch the address snapshot.
- Use async Kafka events (KRaft mode) via a **transactional outbox** for every event that changes state in another service. Consumers must be **idempotent** (dedupe by event ID).
- Checkout saga: reserve stock (Catalog) → create order + payment (Order, local transaction) → confirm or release the reservation. Every step needs a compensation.
- Oversell prevention is enforced in Catalog `inventory` at reservation time (DB-level guarantee), never in the cart.

## Compliance
- India: pincode validation, GST, and the DPDP Act for personal data.
- Account deletion anonymizes personal data. Orders are retained for accounting.
- Never log PII (email, phone, address, PAN, GSTIN) or tokens.

## Local setup
```bash
docker compose -f infra/docker-compose.yml up -d   # postgres, redis
# each service: run from its folder with the project's build tool (Maven/Gradle — TBD, update here)
```
Postgres has one instance locally, with separate databases `catalog_db`, `user_db` and `order_db`.

## Roadmap (services only, current milestone first)
- **M0** Foundations: repo, Docker Compose, conventions, CI
- **M1** Catalog Service, `catalog` module ← start here
- M2 Keycloak + User Service (`identity`, `customer`, `seller`)
- M3 `inventory` module + `cart` module, Resilience4j
- M4 Observability (OpenTelemetry, Prometheus/Grafana/Loki/Tempo)
- M5 Kafka + outbox (seller status → catalog visibility, stock events)
- M6 Checkout saga + Razorpay + notifications
- M7 Search upgrade (optional OpenSearch via CDC)
- M8 Reviews, wishlist (Catalog); promotions (Order)
- M9 Kubernetes + Helm, contract/load/chaos tests, rate limiting, secrets

Do not pull in tools from later milestones (Kafka, Keycloak, OpenSearch) before their milestone unless asked.

## Open questions (ask before assuming)
- Seller model: single owner, or multi-member with roles (`seller_memberships`)?
- Spring Authorization Server as a learning milestone, or Keycloak only?
- Build tool (Maven vs Gradle) and hosting target.