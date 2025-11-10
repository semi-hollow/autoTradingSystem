# Career / Product Brief

## Objectives
1. **Interview readiness** – highlight senior-level Java patterns: Spring Boot 3, Kafka eventing, Redis locks, concurrency guards, Flyway migrations, Dockerized Postgres/Redis/Kafka.
2. **Personal strategy lab** – keep a runnable sandbox for experimenting with automated strategies without deep business complexity.

## Storyline For Interviews
- **Domain framing**: “Strategy-driven trading console where every order flows through Kafka, gets filled asynchronously, and updates positions via Redis-aware services.”
- **Architecture talking points**:
  - Strategy definitions stored as JSON; handler plugins translate configs into orders. Redis + `ConcurrentHashMap` enforce throttles and distribute locks.
  - Kafka topic `trading.orders.submitted` decouples order submission from fills; listeners simulate executions and update Postgres state.
  - OrderService enforces idempotent `clientOrderId`s via Redis and publishes events with Spring Kafka.
  - Frontend is a Vite + React Query single page with strategy/order/position panes refreshing every few seconds.
  - Docker Compose brings up Postgres, Redis, Kafka, backend, and frontend for parity with real environments.
- **Delivery**: demo `Strategy Studio` → trigger run-now → show resulting orders/executions/positions updating via Kafka fills.

## Product Direction
- **Phase 1 (done)**: Core event-driven order flow, strategy engine, Kafka listener fills, lean UI.
- **Phase 2 ideas**:
  1. Backtesting runner that replays historical ticks through the same Kafka topic.
  2. Strategy DSL / schema validation (e.g., JSON Schema + UI form scaffolding).
  3. Multi-broker adapters (REST/WebSocket) and circuit-breaker style risk checks.
  4. Observability bundle (Prometheus/Grafana dashboards for Kafka lag, p95 latency).

Use this doc as a cheat-sheet before interviews or when pitching the side project to recruiters.
