create table users (
    id          bigserial primary key,
    email       varchar(255) unique,
    name        varchar(255),
    created_at  timestamptz not null default now()
);

create table providers (
    id          serial primary key,
    name        varchar(255) unique,
    created_at  timestamptz not null default now()
);

create table user_provider (
    user_id          bigint       not null references users (id) on delete cascade,
    provider_id      int          not null references providers (id),
    user_provider_id varchar(255) not null,
    constraint uq_user_provider unique (user_id, provider_id)
);

create table refresh_tokens (
    id         bigserial primary key,
    token      varchar(255) not null,
    user_id    bigint       not null references users (id) on delete cascade,
    expires_at timestamptz  not null,
    revoked    boolean      not null default false,
    created_at timestamptz  not null default now(),
    constraint uq_refresh_tokens_token unique (token)
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
