CREATE TABLE IF NOT EXISTS account_processing.accounts
(
    id            BIGSERIAL PRIMARY KEY,
    client_id     BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    balance       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    interest_rate NUMERIC(5, 2),
    is_recalc     BOOLEAN        NOT NULL DEFAULT FALSE,
    card_exist    BOOLEAN        NOT NULL DEFAULT FALSE,
    status        VARCHAR(20)    NOT NULL,

    CONSTRAINT fk_accounts_client FOREIGN KEY (client_id)
        REFERENCES client_processing.clients (id),

    CONSTRAINT fk_accounts_product FOREIGN KEY (product_id)
        REFERENCES client_processing.products (id)
);