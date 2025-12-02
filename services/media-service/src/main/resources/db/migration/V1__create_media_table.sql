CREATE TABLE IF NOT EXISTS media (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(1000) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    media_type VARCHAR(50) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO', 'DOCUMENT')),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    alt_text TEXT,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_id ON media(entity_id) WHERE entity_type = 'PRODUCT';
CREATE INDEX idx_entity_type ON media(entity_type);
CREATE INDEX idx_entity_type_id ON media(entity_type, entity_id);
CREATE INDEX idx_primary ON media(is_primary) WHERE is_primary = true;
