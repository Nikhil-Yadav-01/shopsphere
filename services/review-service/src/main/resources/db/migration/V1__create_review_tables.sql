CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    unhelpful_count INTEGER NOT NULL DEFAULT 0,
    moderation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verified_purchase BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_review_product_id ON reviews(product_id);
CREATE INDEX idx_review_user_id ON reviews(user_id);
CREATE INDEX idx_review_moderation_status ON reviews(moderation_status);
CREATE INDEX idx_review_product_user ON reviews(product_id, user_id);
