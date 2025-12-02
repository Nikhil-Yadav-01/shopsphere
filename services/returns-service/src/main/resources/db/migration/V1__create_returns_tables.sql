-- Create return_requests table
CREATE TABLE return_requests
(
    id                       UUID PRIMARY KEY,
    order_id                 UUID           NOT NULL,
    user_id                  UUID           NOT NULL,
    rma_number               VARCHAR(50)    NOT NULL UNIQUE,
    status                   VARCHAR(20)    NOT NULL DEFAULT 'REQUESTED',
    reason                   VARCHAR(255)   NOT NULL,
    description              VARCHAR(1000),
    refund_amount            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    shipping_cost_deductible DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    final_refund_amount      DECIMAL(10, 2),
    requested_at             TIMESTAMP      NOT NULL,
    approved_at              TIMESTAMP,
    rejected_at              TIMESTAMP,
    rejection_reason         VARCHAR(500),
    return_received_at       TIMESTAMP,
    refund_processed_at      TIMESTAMP,
    refund_transaction_id    VARCHAR(100),
    created_at               TIMESTAMP      NOT NULL,
    updated_at               TIMESTAMP      NOT NULL,
    CONSTRAINT fk_order_id FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_return_user_id ON return_requests (user_id);
CREATE INDEX idx_return_order_id ON return_requests (order_id);
CREATE INDEX idx_return_status ON return_requests (status);
CREATE INDEX idx_return_rma_number ON return_requests (rma_number);

-- Create rmas table
CREATE TABLE rmas
(
    id                 UUID PRIMARY KEY,
    return_request_id  UUID        NOT NULL,
    rma_number         VARCHAR(50) NOT NULL UNIQUE,
    status             VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    tracking_number    VARCHAR(100),
    shipping_label_url TEXT,
    expiry_date        TIMESTAMP   NOT NULL,
    is_expired         BOOLEAN     NOT NULL DEFAULT false,
    created_at         TIMESTAMP   NOT NULL,
    updated_at         TIMESTAMP   NOT NULL,
    CONSTRAINT fk_return_request_id FOREIGN KEY (return_request_id) REFERENCES return_requests (id)
);

CREATE INDEX idx_rma_number ON rmas (rma_number);
CREATE INDEX idx_rma_return_request_id ON rmas (return_request_id);
CREATE INDEX idx_rma_status ON rmas (status);
