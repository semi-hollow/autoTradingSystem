# autoTradingSystem

autoTradingSystem is a compact, event-driven trading sandbox that keeps the business scope intentionally tiny so you can concentrate on senior-level engineering topics: Kafka-backed order flow, Redis locks, concurrency-aware strategy automation, and a React console that surfaces just the trading panels that matter.

## Purpose
- Showcase production-inspired primitives (Kafka, Redis, PostgreSQL, distributed locks, scheduling) in a way that is easy to narrate during interviews or architecture reviews.
- Provide a safe lab for iterating on strategy automation patterns—manual orders and scheduled strategies share the same pipeline, so every improvement carries over end-to-end.
- Deliver a reproducible environment (Docker Compose + Gradle + Vite) that can run locally or on a remote dev box with identical topology.

## System Highlights
- **Strategy Studio**: JSON-defined strategies (fixed-amount DCA, grid buy) with cron windows, enable/disable flags, and throttles backed by `StrategyThrottleRegistry`.
- **Event-Driven Order Lifecycle**: `OrderService` enforces Redis idempotency, persists orders, and publishes to Kafka topic `trading.orders.submitted`; asynchronous listeners simulate fills, update executions/positions, and emit latency metrics.
- **Concurrency & Risk Controls**: Redis locks plus in-memory throttles prevent duplicate strategy runs, while `clientOrderId` uniqueness guards manual or automated flows.
- **Portfolio State & Observability**: Positions, executions, and optional latency metrics are persisted in PostgreSQL; lightweight controllers expose REST endpoints for strategies/orders/executions/positions/metrics.
- **Lean React Console**: React 18 + Ant Design + React Query render strategy, order, and position panes with 3-second polling so you can narrate the flow without UI bloat.

## Architecture & Tech Stack
- **Backend**: Java 17, Spring Boot 3 (Web, Data JPA, Scheduling, Validation), Spring Kafka, Redis client, Flyway migrations, Testcontainers-backed tests, Gradle Kotlin DSL build.
- **Data & Messaging**: PostgreSQL 16 for relational state, Redis 7 for locks/idempotency, Kafka 3.7 (Zookeeper 3.9) for the order event bus; optional metrics table ready for future observability work.
- **Frontend**: React 18, TypeScript, Vite tooling, Ant Design components, React Query for polling/mutations, and axios-based API helpers.
- **Ops**: Docker Compose spins up Zookeeper, Kafka, Postgres, Redis, backend, and frontend with sensible defaults; `.env` style overrides map 1:1 to `application.yml`.

## Engineering Notes
- Strategy execution is delegated to handler plugins (`FixedAmountDcaHandler`, `GridBuyStrategyHandler`) so you can add new strategy shapes without touching scheduling code.
- Kafka listeners (`OrderEventListener`, `OrderFillProcessor`) rely on an `InMemoryPriceCache` to simulate fills and keep throughput predictable for demos.
- `StrategyScheduler` combines cron expressions, Redis locks, and throttles to ensure strategies trigger safely even across multiple nodes.
- REST surface area stays small on purpose: `POST /api/strategies`, `POST /api/strategies/{id}/run-now`, `POST /api/orders`, `GET /api/orders`, `GET /api/executions`, `GET /api/positions`, and supporting lookup endpoints for accounts/instruments/metrics.
- Latency metrics are recorded through `LatencyMetricService` to illustrate how you would extend the system with Prometheus or custom dashboards later.

## Runbook
```bash
# Full stack (needs Docker)
docker-compose up --build

# Backend only (expects local Postgres/Redis/Kafka)
cd backend
./gradlew bootRun

# Frontend only (point VITE_BACKEND_URL to your API)
cd frontend
npm install
npm run dev
```

See `application.yml`, `docker-compose.yml`, and `trading-mvp-brief.md` for environment variables, topic names, and data model references. `docs/career-brief.md` and `trading-mvp-brief.md` double as talking-point aides when presenting the system.
