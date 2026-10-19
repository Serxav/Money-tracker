-- Cuentas (cada una con una única divisa) y categorías de ingreso/gasto.
CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(60)  NOT NULL,
    currency        VARCHAR(3)   NOT NULL,
    initial_balance BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT accounts_currency_format CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT accounts_initial_balance_range
        CHECK (initial_balance BETWEEN -1000000000000000 AND 1000000000000000)
);
CREATE UNIQUE INDEX accounts_user_name_uq ON accounts (user_id, lower(name));

CREATE TABLE categories (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       VARCHAR(40) NOT NULL,
    type       VARCHAR(10) NOT NULL,
    icon       VARCHAR(40),
    color      VARCHAR(7),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT categories_type_valid CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT categories_color_format CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$')
);
CREATE UNIQUE INDEX categories_user_type_name_uq ON categories (user_id, type, lower(name));
