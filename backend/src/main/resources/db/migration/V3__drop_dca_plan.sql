alter table orders
    drop column if exists plan_id;

drop table if exists dca_plan cascade;
