CREATE TABLE instrument (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL UNIQUE,
    currency VARCHAR(12) NOT NULL
);

CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    base_currency VARCHAR(12) NOT NULL,
    cash_available NUMERIC(18, 4) NOT NULL DEFAULT 0
);

CREATE TABLE dca_plan (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES account (id),
    instrument_id BIGINT NOT NULL REFERENCES instrument (id),
    cash_amount NUMERIC(18, 4) NOT NULL,
    cron VARCHAR(64) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    last_run_at TIMESTAMP WITH TIME ZONE,
    holiday_policy VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    client_order_id VARCHAR(64) NOT NULL,
    plan_id BIGINT REFERENCES dca_plan (id),
    account_id BIGINT NOT NULL REFERENCES account (id),
    instrument_id BIGINT NOT NULL REFERENCES instrument (id),
    side VARCHAR(8) NOT NULL,
    type VARCHAR(16) NOT NULL,
    qty NUMERIC(18, 6),
    cash_amount NUMERIC(18, 4),
    limit_price NUMERIC(18, 4),
    status VARCHAR(32) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_orders_client_order_id UNIQUE (client_order_id)
);

CREATE INDEX idx_orders_account_created ON orders (account_id, created_at DESC);

CREATE TABLE executions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id),
    price NUMERIC(18, 4) NOT NULL,
    qty NUMERIC(18, 6) NOT NULL,
    fee NUMERIC(18, 4) NOT NULL DEFAULT 0,
    ts TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_executions_order_id ON executions (order_id);

CREATE TABLE positions (
    account_id BIGINT NOT NULL REFERENCES account (id),
    instrument_id BIGINT NOT NULL REFERENCES instrument (id),
    qty NUMERIC(18, 6) NOT NULL,
    avg_price NUMERIC(18, 4) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (account_id, instrument_id)
);

CREATE TABLE metrics_latency (
    id BIGSERIAL PRIMARY KEY,
    stage VARCHAR(32) NOT NULL,
    latency_ms BIGINT NOT NULL,
    ts TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
