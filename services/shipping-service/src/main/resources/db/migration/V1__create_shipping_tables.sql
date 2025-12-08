-- Shipping tables for ShopSphere Shipping Service

CREATE TABLE IF NOT EXISTS shipments
(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id UUID NOT NULL,
    tracking_number VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL,
    carrier VARCHAR(100),
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipment_items
(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    shipment_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_shipment_items_shipment_id FOREIGN KEY (shipment_id) REFERENCES shipments (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shipment_events
(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    shipment_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipment_events_shipment_id FOREIGN KEY (shipment_id) REFERENCES shipments (id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_shipments_order_id ON shipments (order_id);
CREATE INDEX IF NOT EXISTS idx_shipments_tracking_number ON shipments (tracking_number);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments (status);
CREATE INDEX IF NOT EXISTS idx_shipment_items_shipment_id ON shipment_items (shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_events_shipment_id ON shipment_events (shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_events_event_time ON shipment_events (event_time);
