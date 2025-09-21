CREATE TABLE IF NOT EXISTS client_processing.client_product
(
    id         BIGSERIAL   PRIMARY KEY,
    client_id  BIGINT      NOT NULL UNIQUE,
    product_id BIGINT      NOT NULL UNIQUE,
    open_date  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    close_date TIMESTAMP,
    status     VARCHAR(20) NOT NULL,

    CONSTRAINT fk_client_products_client FOREIGN KEY (client_id)
        REFERENCES client_processing.clients (id),

    CONSTRAINT fk_client_products_product FOREIGN KEY (product_id)
        REFERENCES client_processing.products (id)
);