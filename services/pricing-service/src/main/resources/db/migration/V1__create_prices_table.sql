CREATE TABLE prices
(
    id                  BIGSERIAL PRIMARY KEY,
    product_id          VARCHAR(255)   NOT NULL UNIQUE,
    base_price          DECIMAL(19, 2) NOT NULL,
    selling_price       DECIMAL(19, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2),
    currency            VARCHAR(10)    NOT NULL,
    effective_from      TIMESTAMP,
    effective_to        TIMESTAMP,
    created_at          TIMESTAMP      NOT NULL,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_prices_product_id ON prices (product_id);
