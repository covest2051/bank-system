CREATE TABLE IF NOT EXISTS credit_processing.product_registry
(
    id            BIGSERIAL PRIMARY KEY,
    client_id     BIGINT        NOT NULL,
    account_id    BIGINT        NOT NULL,
    product_id    BIGINT        NOT NULL,
    interest_rate NUMERIC(5, 2) NOT NULL,
    open_date     TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_product_registry_client FOREIGN KEY (client_id)
        REFERENCES client_processing.clients (id),

    CONSTRAINT fk_product_registry_account FOREIGN KEY (account_id)
        REFERENCES account_processing.accounts (id),

    CONSTRAINT fk_product_registry_product FOREIGN KEY (product_id)
        REFERENCES client_processing.products (id)
);
