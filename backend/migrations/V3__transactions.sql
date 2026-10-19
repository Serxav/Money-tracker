-- Movimientos (ingresos y gastos) y claves de idempotencia para crearlos sin duplicados.
CREATE TABLE transactions (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id  UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    category_id UUID         NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    type        VARCHAR(10)  NOT NULL,
    amount      BIGINT       NOT NULL,
    currency    VARCHAR(3)   NOT NULL,
    date        DATE         NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT transactions_type_valid CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT transactions_amount_range CHECK (amount > 0 AND amount <= 1000000000000000),
    CONSTRAINT transactions_currency_format CHECK (currency ~ '^[A-Z]{3}$')
);
CREATE INDEX transactions_user_date_idx ON transactions (user_id, date DESC, created_at DESC);
CREATE INDEX transactions_account_idx ON transactions (account_id);
CREATE INDEX transactions_category_idx ON transactions (category_id);

CREATE TABLE idempotency_keys (
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    idem_key       VARCHAR(64) NOT NULL,
    request_hash   VARCHAR(64) NOT NULL,
    transaction_id UUID        NOT NULL REFERENCES transactions (id) ON DELETE CASCADE,
    created_at     TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, idem_key)
);
CREATE INDEX idempotency_keys_created_idx ON idempotency_keys (created_at);
