CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    card_limit NUMERIC(19, 2) NOT NULL,
    used_funds NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL
);