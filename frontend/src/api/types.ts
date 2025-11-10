export type OrderStatus =
  | "PENDING_SUBMIT"
  | "SUBMITTED"
  | "PARTIALLY_FILLED"
  | "FILLED"
  | "REJECTED"
  | "CANCELED";

export interface Order {
  id: number;
  clientOrderId: string;
  accountId: number;
  instrumentId: number;
  strategyId?: number;
  strategyName?: string;
  strategyTag?: string;
  side: "BUY" | "SELL";
  type: "MARKET" | "LIMIT";
  qty?: number;
  cashAmount?: number;
  limitPrice?: number;
  status: OrderStatus;
  reason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Execution {
  id: number;
  orderId: number;
  price: number;
  qty: number;
  fee: number;
  timestamp: string;
}

export interface Position {
  accountId: number;
  instrumentId: number;
  qty: number;
  avgPrice: number;
  updatedAt: string;
}

export type StrategyType = "FIXED_AMOUNT_DCA" | "GRID_BUY";

export type StrategyStatus = "ACTIVE" | "PAUSED" | "ARCHIVED";

export interface Strategy {
  id: number;
  name: string;
  description?: string;
  accountId: number;
  instrumentId: number;
  type: StrategyType;
  status: StrategyStatus;
  cron: string;
  startAt: string;
  endAt?: string;
  lastRunAt?: string;
  createdAt: string;
  updatedAt: string;
  parameters: Record<string, unknown>;
}

export type StrategyTrigger = "MANUAL" | "SCHEDULED";

export interface StrategyRunResponse {
  strategy: Strategy;
  trigger: StrategyTrigger;
  orders: Order[];
}
