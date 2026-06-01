CREATE TABLE card_transactions (
    id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES credit_cards(id)
);