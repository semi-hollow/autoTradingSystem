# Trading Build Brief
_Updated: 2025-11-02_

## 1) Goals
- **Primary:** Build a compact yet production-inspired trading sandbox to practice senior-level Java skills (Kafka, Redis, concurrency, strategy automation).
- **Secondary:** Keep business logic minimal—only features that surface real-world engineering challenges and are easy to explain in interviews.

## 2) Scope
- Accounts & instruments (demo data only).
- Strategy definitions (fixed-amount DCA, grid buy) with JSON parameters, CRON windows, status toggles, Redis locks, and in-memory throttles.
- Manual orders + strategy-triggered orders share one order pipeline.
- Kafka-driven lifecycle: OrderService → Kafka topic → async fill listener → executions + positions.
- Minimal UI (strategies, orders, positions) with React Query polling every 3 seconds.

## 3) Tech Stack
- **Backend:** Java 17, Spring Boot 3 (Web/Data/Scheduling), Spring Kafka, Redis, Flyway, Testcontainers, Gradle Kotlin DSL.
- **Storage:** PostgreSQL 16 for relational state, Redis for idempotency and distributed locks.
- **Messaging:** Kafka 3.x (topic `trading.orders.submitted`) with Spring Kafka listener for fill simulation.
- **Frontend:** React 18 + TypeScript + Vite + Ant Design + React Query.
- **Packaging:** Docker Compose (Postgres, Redis, Kafka, Zookeeper, backend, frontend).

## 4) Data Model
- `instrument(id, symbol, currency)`
- `account(id, name, base_currency, cash_available)`
- `strategy_definition(id, name, account_id, instrument_id, type, status, cron, parameters_json, start_at, end_at, last_run_at)`
- `orders(id, client_order_id, account_id, instrument_id, strategy_id, strategy_tag, side, type, qty, cash_amount, limit_price, status, created_at)`
- `executions(id, order_id, price, qty, fee, ts)`
- `positions(account_id, instrument_id, qty, avg_price, updated_at)`
- `metrics_latency(id, stage, latency_ms, ts)` *(reserved for future observability work)*

Indexes: `orders(client_order_id unique)`, `orders(account_id, created_at DESC)`, `executions(order_id)`, `positions(account_id, instrument_id unique)`.

## 5) REST Endpoints
- `POST /api/strategies` – create strategy definition.
- `GET /api/strategies` – list strategies.
- `POST /api/strategies/{id}/run-now` – trigger strategy immediately (throttled).
- `POST /api/orders` – submit manual order.
- `POST /api/orders/{id}/cancel` – cancel pending order.
- `GET /api/orders` – list orders (status/account filters optional).
- `GET /api/executions` – list executions by orderId.
- `GET /api/positions` – current positions snapshot.

## 6) Event Flow
1. Strategy scheduler (Quartz cron) checks due strategies, acquires Redis lock + throttle guard, and generates order requests.
2. `OrderService` validates input, enforces Redis idempotency, persists the order, and publishes `OrderSubmittedEvent` to Kafka.
3. Kafka listener simulates fills using a concurrent price cache, persists executions, updates cash + positions, and marks the order `FILLED`.
4. React console polls `/strategies`, `/orders`, `/positions` every 3 seconds for live status.

## 7) Suggested Iteration Order
1. Data model + Flyway migrations + repositories.
2. OrderService with Redis idempotency + Kafka publisher.
3. Kafka listener + execution/position updater.
4. Strategy engine (handlers + throttles + scheduler).
5. Minimal React console (strategies, orders, positions).
6. Docker Compose + docs + smoke tests.

This brief replaces the older DCA-heavy backlog and keeps everyone aligned on the lean, technology-first focus.
