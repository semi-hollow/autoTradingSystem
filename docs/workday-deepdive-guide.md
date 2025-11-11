# 工作时间速读指南：autoTradingSystem 架构速通手册

> 目标：在零散的工作时段内，用最少的上下文切换快速掌握 autoTradingSystem 的核心模块、文件分布与运行路径，方便在面试或复盘时随时调用。

## 1. 起步必读（20 分钟）
| 顺序 | 文档 | 你会得到的内容 |
| --- | --- | --- |
| 1 | `README.md` → Purpose & Highlights | 项目定位、核心卖点、运行方式（对面试陈述最有用的 3 个段落）。 |
| 2 | `trading-mvp-brief.md` → Scope、Data Model、Event Flow | 数据库表、Kafka 主题、订单生命周期，一次性建立端到端心智模型。 |
| 3 | `docs/career-brief.md` → Storyline | 面向面试官的叙事脚本，帮助串联策略 → 订单 → 成交 → 前端。 |

阅读时保持 README 打开，将 `trading-mvp-brief.md` 与源码左右分屏对照，可快速确认每个流程对应的类。

## 2. 代码分布速查表
### 2.1 后端（`backend/src/main/java/com/autotrading/tradingmvp/`）
```
config/            Spring & Kafka/Redis 配置、序列化设置。
controller/        REST API：策略、订单、执行、持仓、账户等入口。
domain/            JPA 实体与仓储接口，命名与表结构一致。
dto/               前后端交互的数据模型（请求/响应/视图）。
adapter/           BrokerAdapter、PriceFeed 等外部系统抽象。
service/
  OrderService     订单创建、Redis 幂等校验、Kafka 发布。
  Strategy...      策略调度、执行与节流，含 `strategy/` 策略 handler。
orderflow/         Kafka 消费者：监听提交事件、模拟成交、更新执行/持仓。
scheduler/         `StrategyScheduler` 负责 CRON 触发与 Redis 锁。
```
把 `OrderService` → `orderflow` → `domain` 这一条链串起来，就能看到从下单到更新数据库的关键步骤。

### 2.2 前端（`frontend/src/`）
```
api/
  client.ts        axios 客户端，统一后端地址与错误处理。
  resources.ts     针对策略、订单、执行、持仓的 REST 包装函数。
views/
  StrategiesView   策略列表、新建、立即执行，触发后端 `/api/strategies`。
  OrdersView       展示订单与执行，轮询 `/api/orders`、`/api/executions`。
  PositionsView    展示当前持仓、盈亏。
App.tsx            三个 Tab 面板入口，React Query 轮询策略/订单。
```
`api/` 与 `views/` 命名保持和后端 REST 控制器一致，便于在两个仓之间跳转。

## 3. 快速解构架构（40 分钟）
1. **策略调度**（10 分钟）：`scheduler/StrategyScheduler` → `service/StrategyExecutionService` → `service/strategy/*Handler`。
2. **订单提交流水线**（15 分钟）：`controller/OrderController` → `service/OrderService` → Kafka 主题 `trading.orders.submitted` → `orderflow/OrderEventListener`。
3. **成交与持仓更新**（10 分钟）：`orderflow/OrderFillProcessor` 与 `domain/Position`、`domain/Execution`。
4. **前端联调**（5 分钟）：`frontend/src/api/resources.ts` 对照相应控制器，确认字段与轮询节奏。

每完成一步，在 `trading-mvp-brief.md` 中找到对应的章节，强化记忆并准备面试时的讲述路径。

## 4. 文件夹之间的“跳板”
- **策略 → 订单**：在策略 handler 中查找 `submitOrder`，跳转即到 `OrderService`。
- **订单 → Kafka → 成交**：在 `OrderService` 搜索 `kafkaTemplate.send`，方法参数直接给出主题名；复制主题名去 `orderflow` 搜索监听器。
- **前端 → 后端**：在 `frontend/src/api/resources.ts` 搜索 `/api/` 字符串；每个函数名与后端 `controller` 同步，使用 IDE 的“查找文件”跳转。
- **数据库结构**：`backend/src/main/resources/db/migration` 下的 Flyway 脚本与 `domain` 实体一一对应。先看 SQL，再对照实体注解。

## 5. 工作间隙的碎片化学习方案
| 时间块 | 操作 | 目标 |
| --- | --- | --- |
| 15 分钟 | 重读 README Highlights + 对照 `OrderService` | 刷新卖点与主流程，用于回答“为什么这样设计”。 |
| 10 分钟 | 查看 `StrategyScheduler` 最近 commit diff | 了解调度与锁的更新，准备追问。 |
| 20 分钟 | 打开前端三大视图，跟踪一次策略执行 → 订单轮询 → 持仓更新 | 建立 Demo 讲解脚本。 |
| 5 分钟  | 浏览 `docker-compose.yml`，记录端口与依赖 | 面试常见问题：“如何一键启动？” |

## 6. 常用命令速贴
```bash
# 列出后端核心类，便于快速定位
rg "class Order" backend/src/main/java

# 查看 Kafka 事件流中涉及的 topic 名称
rg "trading\.orders" -n backend/src/main/java

# 前端查看某个 API 在哪些视图里使用
rg "getOrders" frontend/src
```
把这些命令加入工作笔记，碎片时间打开终端即可快速找回上下文。

## 7. 下一步深挖建议
- **写一条 demo 策略**：修改 `docs/trading-mvp-brief.md` 中的参数，亲手调用 `POST /api/strategies`，对照数据库表验证字段。
- **演练扩展点**：阅读 `service/strategy/StrategyHandler` 接口，思考如何新增一个“止盈止损”策略；准备在面试时回答扩展性问题。
- **监控话术**：在 `LatencyMetricService` 中挑两句代码，准备回答“如何对接 Prometheus”。

通过以上路线，在上班的零散时间也能逐步拼出全貌，并随时切换到面试叙事模式。
