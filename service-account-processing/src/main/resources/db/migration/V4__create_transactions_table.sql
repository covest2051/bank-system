CREATE TABLE IF NOT EXISTS account_processing.transactions
(
    id         BIGSERIAL PRIMARY KEY,
    account_id BIGINT         NOT NULL,
    card_id    BIGINT,
    type       VARCHAR(50)    NOT NULL,
    amount     NUMERIC(19, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    timestamp  TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id)
        REFERENCES account_processing.accounts (id),

    CONSTRAINT fk_transactions_card FOREIGN KEY (card_id)
        REFERENCES account_processing.cards (id)
);
