create table if not exists strategy_definition (
    id              bigserial primary key,
    name            varchar(128) not null,
    description     varchar(512),
    account_id      bigint not null references account(id),
    instrument_id   bigint not null references instrument(id),
    type            varchar(32) not null,
    status          varchar(32) not null,
    cron            varchar(64) not null,
    start_at        timestamptz not null,
    end_at          timestamptz,
    last_run_at     timestamptz,
    parameters_json text not null,
    created_at      timestamptz,
    updated_at      timestamptz
);

create index if not exists idx_strategy_definition_status on strategy_definition(status);
create index if not exists idx_strategy_definition_account on strategy_definition(account_id);

alter table orders
    add column if not exists strategy_tag varchar(64);

alter table orders
    add column if not exists strategy_id bigint;

alter table orders
    add constraint if not exists fk_orders_strategy
        foreign key (strategy_id) references strategy_definition(id);

create index if not exists idx_orders_strategy on orders(strategy_id);
