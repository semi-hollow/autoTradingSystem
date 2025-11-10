# autoTradingSystem

Minimal-yet-serious auto-trading sandbox built to highlight the core skills expected from a senior Java engineer: distributed locking, Kafka-based order flow, strategy-driven automation, and an opinionated React console. Business scope stays tiny so you can focus on deep technical narratives in interviews.

## What's Included
- **Strategy Studio** - create reusable strategy definitions (fixed-amount DCA, grid buy) with JSON parameters, cron windows, and throttle guards backed by `ConcurrentHashMap`.
- **Event-Driven Order Lifecycle** - every order publishes to Kafka (`trading.orders.submitted`), is filled asynchronously, and updates executions + positions.
- **Redis & Concurrency Controls** - Redis handles idempotency + distributed locks while custom throttles showcase advanced `ConcurrentMap` usage.
- **Lean Frontend** - only the panels that matter (strategies, orders, positions) built with React + TS + Ant Design.

## Tech Stack Highlights
- **Backend:** Java 17, Spring Boot 3 (Web/Data/Scheduling), Spring Kafka, Redis, Flyway, Testcontainers, Gradle Kotlin DSL.
- **Data & Messaging:** PostgreSQL 16 for persistence, Redis for locks/caching, Kafka 3.x for order/event streams.
- **Frontend:** React 18, TypeScript, Vite, Ant Design, React Query for polling/mutations.
- **Ops:** Docker Compose topology (Postgres, Redis, Kafka, Zookeeper, backend, frontend) for one-command spin up.

## Running Locally
```bash
# Start infra + apps (requires Docker)
docker-compose up --build

# Or run services individually
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

Configure Kafka/Redis/Postgres via environment variables (see `application.yml`). Default dev setup expects everything on localhost (see compose file for reference).

Use this project as a talking-point hub: explain the event pipeline, concurrency guards, and how each subsystem would evolve in a production trading stack. Keep extending strategies or wiring a real broker adapter when you're ready.
