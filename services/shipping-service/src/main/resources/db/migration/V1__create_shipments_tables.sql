-- Create shipments table
CREATE TABLE IF NOT EXISTS shipments (
    id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    carrier VARCHAR(50) NOT NULL,
    shipping_cost DECIMAL(10, 2) NOT NULL,
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    recipient_full_name VARCHAR(255),
    recipient_phone VARCHAR(20),
    recipient_address_line1 VARCHAR(255),
    recipient_address_line2 VARCHAR(255),
    recipient_city VARCHAR(100),
    recipient_state VARCHAR(50),
    recipient_postal_code VARCHAR(20),
    recipient_country VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipments_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create tracking_events table
CREATE TABLE IF NOT EXISTS tracking_events (
    id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL,
    event_code VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    location VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracking_events_shipment_id FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE
);

-- Create indices for better query performance
CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);
CREATE INDEX idx_tracking_events_shipment_id ON tracking_events(shipment_id);
CREATE INDEX idx_tracking_events_occurred_at ON tracking_events(occurred_at);
