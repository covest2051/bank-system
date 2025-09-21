CREATE TABLE IF NOT EXISTS client_processing.products
(
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    product_key     VARCHAR(4)   NOT NULL,
    create_date     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    product_id      VARCHAR(50)
);