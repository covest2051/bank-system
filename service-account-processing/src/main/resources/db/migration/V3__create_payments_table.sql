CREATE TABLE IF NOT EXISTS account_processing.payments
(
    id             BIGSERIAL PRIMARY KEY,
    account_id     BIGINT         NOT NULL,
    payment_date   TIMESTAMP      NOT NULL,
    amount         NUMERIC(12, 2) NOT NULL,
    is_credit BOOLEAN NOT NULL,
    payed_at TIMESTAMP,
    type VARCHAR(50) NOT NULL,

    CONSTRAINT fk_payments_account  FOREIGN KEY (account_id)
        REFERENCES account_processing.accounts (id)
);
