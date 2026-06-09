CREATE TABLE "users" (
    id         UUID PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(10)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE "credit_cards" (
    id UUID PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    card_limit NUMERIC(19, 2) NOT NULL,
    used_funds NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_customerId foreign key (customer_id) references "users"(id)
);

CREATE TABLE "card_transactions" (
    id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES "credit_cards"(id)
);
