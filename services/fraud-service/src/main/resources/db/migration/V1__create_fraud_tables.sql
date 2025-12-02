CREATE TABLE fraud_cases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    fraud_score INTEGER NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(500),
    billing_address VARCHAR(500),
    reason VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    decision_notes VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_fraud_user_id ON fraud_cases(user_id);
CREATE INDEX idx_fraud_order_id ON fraud_cases(order_id);
CREATE INDEX idx_fraud_status ON fraud_cases(status);
CREATE INDEX idx_fraud_risk_level ON fraud_cases(risk_level);
