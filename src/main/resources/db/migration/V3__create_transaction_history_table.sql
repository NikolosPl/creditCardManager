CREATE TABLE card_operation (
    id           BIGSERIAL PRIMARY KEY,
    card_id      BIGINT        REFERENCES credit_cards(id) ON DELETE CASCADE,
    type         VARCHAR(20)   NOT NULL CHECK (type IN ('ISSUE', 'BLOCK', 'UNBLOCK', 'LIMIT_CHANGE')),
    description  TEXT,
    amount       NUMERIC(12,2),
    performed_by VARCHAR(50),
    timestamp    TIMESTAMP     NOT NULL DEFAULT NOW()
);