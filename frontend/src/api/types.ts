export interface Plan {
  id: number;
  accountId: number;
  instrumentId: number;
  cashAmount: number;
  cron: string;
  startAt: string;
  endAt?: string;
  status: "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
  lastRunAt?: string;
  holidayPolicy: "SKIP" | "NEXT_BUSINESS_DAY";
}

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
  planId?: number;
  accountId: number;
  instrumentId: number;
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

export interface LatencyMetric {
  stage: string;
  latencyMs: number;
  timestamp: string;
}
