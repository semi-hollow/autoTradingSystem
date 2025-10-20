# Trading MVP Brief (for Codex ingestion)
_Last updated: 2025-10-19_

## 1) 背景与目标
- 个人目标：练 Java、做成可演示的作品，同时满足自己投资用的自动定投与简易策略下单。
- 范围主张：功能优先，合规/安全先不展开；先做单体应用跑通闭环，后续再进化。
- 期望节奏：约两周完成可演示版（MVP）。

## 2) 功能范围（MVP）
### 账户与品种
- 本地模拟账户（币种与可用现金），少量标的（VOO、QQQ 等）。

### 自动定投（DCA）
- 新建计划：标的、固定金额、CRON 频率、起止日期、节假日顺延（简化处理）。
- 到点自动下单，记录订单与成交，更新持仓。
- “立刻执行一次”手动触发。

### 简易策略下单
- 两种内置策略：
  1) 固定金额买入（市价）。
  2) 网格买入（每跌 N% 以 M 美元买入）。
- 支持市价/限价、撤单。

### 订单/持仓/报表
- 订单状态机：PENDING_SUBMIT → SUBMITTED → PARTIALLY_FILLED/FILLED/REJECTED/CANCELED。
- 实时持仓与盈亏（按最新价或上次成交价）。
- 简单报表：当日下单数、成交额、策略触发次数。

### 可观测（最小）
- 延迟四段：决策→提交、提交→ACK、ACK→成交、回报→入账，记录 P50/P95。
- 最少量日志与指标查询接口。

### 前端（单页）
- 定投计划列表与新建向导。
- 订单/成交/持仓三张表，3 秒轮询刷新。

## 3) 技术栈（极简能打）
- 后端：Java 17、Spring Boot 3（Web/Actuator/Scheduling）、Spring Data JPA、Flyway、JUnit5/Testcontainers。
- 存储：PostgreSQL 16。
- 前端：React + TypeScript、Vite、Ant Design、React Query（轮询）。
- 适配：BrokerAdapter 接口 + adapter-stub（可配置延迟/部分成交/拒单注入）。
- 打包：Docker/Compose（app + Postgres）。

## 4) 数据模型（表）
- instrument(id, symbol, currency)
- account(id, name, base_currency, cash_available)
- dca_plan(id, account_id, instrument_id, cash_amount, cron, start_at, end_at, status, last_run_at, holiday_policy)
- orders(id, client_order_id, plan_id, account_id, instrument_id, side, type, qty, cash_amount, limit_price, status, created_at)
- executions(id, order_id, price, qty, fee, ts)
- positions(account_id, instrument_id, qty, avg_price, updated_at)
- （可选）metrics(id, stage, latency_ms, ts)

索引：orders(client_order_id unique)、orders(account_id, created_at)，executions(order_id)，positions(account_id, instrument_id unique)。

## 5) 关键接口（REST）
- POST /api/plans            新建定投计划
- GET  /api/plans            查询计划
- POST /api/plans/{id}/run-now   立刻执行一次
- POST /api/orders           直接下单（策略入口）
- POST /api/orders/{id}/cancel   撤单
- GET  /api/orders           订单列表（支持筛选）
- GET  /api/executions       成交查询（按 orderId）
- GET  /api/positions        当前持仓
- GET  /api/metrics/latency  延迟统计

## 6) 适配器契约（示意）
```java
public interface BrokerAdapter { 
  Ack placeOrder(PlaceOrderCmd cmd); 
  void cancelOrder(String clientOrderId); 
}
```
- adapter-stub：p50Latency、partialFillRate、rejectRate 可配置；用调度线程异步回报。

## 7) 调度与流程
1. Scheduler 每分钟扫描到期的 dca_plan；现金不足则记录并顺延。
2. 生成市价单（按金额），clientOrderId = planId + epochMillis；落库为 PENDING_SUBMIT。
3. 调用适配器→SUBMITTED；异步回报写 executions，更新 positions 与现金。
4. 记录四段延迟；更新 plan.last_run_at。

## 8) 两周节奏
- D1–D2：项目骨架、三表（Order/Exec/Position）与 JPA。
- D3–D4：BrokerAdapter + adapter-stub；状态机与幂等键。
- D5–D6：PlanService + Scheduler + run-now。
- D7–D8：前端三表 + 计划向导。
- D9–D10：网格策略 + 价格模拟器。
- D11–D12：延迟指标与接口。
- D13–D14：小压测与 README。

## 9) 使用方式（给 Codex）
- 读取本文件，熟悉目标与接口；按“两周节奏”给出每日任务清单与代码骨架；从 D1 开始落实代码模板。
