CREATE TABLE IF NOT EXISTS account_processing.cards
(
    id             BIGSERIAL PRIMARY KEY,
    account_id     BIGINT      NOT NULL,
    card_id        VARCHAR(30) NOT NULL UNIQUE,
    payment_system VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,

    CONSTRAINT fk_cards_account FOREIGN KEY (account_id)
        REFERENCES account_processing.accounts (id)
);
