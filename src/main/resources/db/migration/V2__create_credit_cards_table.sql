CREATE TABLE credit_cards (
    id              BIGSERIAL PRIMARY KEY,
    card_number     VARCHAR(19)   UNIQUE NOT NULL,
    holder_name     VARCHAR(100)  NOT NULL,
    user_id         BIGINT        REFERENCES users(id) ON DELETE SET NULL,
    credit_limit    NUMERIC(12,2) NOT NULL,
    current_balance NUMERIC(12,2) NOT NULL DEFAULT 0,
    status          VARCHAR(10)   NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED')),
    issued_at       TIMESTAMP     NOT NULL,
    expires_at      TIMESTAMP     NOT NULL
);