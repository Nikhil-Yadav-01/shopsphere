CREATE TABLE coupons
(
    id                    BIGSERIAL PRIMARY KEY,
    code                  VARCHAR(50)    NOT NULL UNIQUE,
    description           VARCHAR(500),
    discount_type         VARCHAR(50)    NOT NULL,
    discount_value        DECIMAL(10, 2) NOT NULL,
    max_discount          DECIMAL(10, 2),
    min_order_value       DECIMAL(10, 2),
    usage_limit           INTEGER,
    valid_from            TIMESTAMP      NOT NULL,
    valid_until           TIMESTAMP      NOT NULL,
    is_active             BOOLEAN        NOT NULL DEFAULT true,
    max_uses_per_user     INTEGER,
    applicable_categories TEXT,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               BOOLEAN        NOT NULL DEFAULT false
);

CREATE TABLE coupon_redemptions
(
    id              BIGSERIAL PRIMARY KEY,
    coupon_id       BIGINT         NOT NULL,
    user_id         BIGINT         NOT NULL,
    order_id        BIGINT,
    discount_amount DECIMAL(10, 2) NOT NULL,
    redeemed_at     TIMESTAMP      NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN        NOT NULL DEFAULT false,
    FOREIGN KEY (coupon_id) REFERENCES coupons (id)
);

CREATE INDEX idx_coupon_code ON coupons (code);
CREATE INDEX idx_coupon_active ON coupons (is_active);
CREATE INDEX idx_coupon_redemption_coupon_id ON coupon_redemptions (coupon_id);
CREATE INDEX idx_coupon_redemption_user_id ON coupon_redemptions (user_id);
CREATE INDEX idx_coupon_redemption_order_id ON coupon_redemptions (order_id);
