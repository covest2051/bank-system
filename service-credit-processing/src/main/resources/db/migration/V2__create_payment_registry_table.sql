CREATE TABLE IF NOT EXISTS credit_processing.payment_registry
(
    id                      BIGSERIAL PRIMARY KEY,
    product_registry_id     BIGINT         NOT NULL,
    payment_date            TIMESTAMP      NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL,
    interest_rate_amount    NUMERIC(19, 2) NOT NULL,
    debt_amount             NUMERIC(19, 2) NOT NULL,
    expired                 BOOLEAN        NOT NULL DEFAULT FALSE,
    payment_expiration_date TIMESTAMP      NOT NULL,

    CONSTRAINT fk_payment_registry_product FOREIGN KEY (product_registry_id)
        REFERENCES credit_processing.product_registry (id)
);
