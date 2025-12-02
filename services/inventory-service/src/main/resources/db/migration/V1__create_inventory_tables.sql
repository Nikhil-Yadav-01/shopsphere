-- Inventory table
CREATE TABLE inventory
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    product_id        UUID        NOT NULL UNIQUE,
    sku               VARCHAR(50) NOT NULL UNIQUE,
    quantity          INTEGER     NOT NULL DEFAULT 0,
    reserved_quantity INTEGER     NOT NULL DEFAULT 0,
    warehouse_id      UUID        NOT NULL,
    reorder_level     INTEGER     NOT NULL DEFAULT 10,
    reorder_quantity  INTEGER     NOT NULL DEFAULT 50,
    last_restocked_at TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT chk_reserved_quantity_non_negative CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_reserved_not_exceed_quantity CHECK (reserved_quantity <= quantity)
);

-- Stock movements table
CREATE TABLE stock_movements
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    inventory_id UUID        NOT NULL REFERENCES inventory (id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL CHECK (type IN ('IN', 'OUT', 'RESERVED', 'RELEASED')),
    quantity     INTEGER     NOT NULL,
    reason       VARCHAR(255),
    reference_id UUID,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movement_quantity_positive CHECK (quantity > 0)
);

-- Indexes for inventory table
CREATE INDEX idx_inventory_product_id ON inventory (product_id);
CREATE INDEX idx_inventory_sku ON inventory (sku);
CREATE INDEX idx_inventory_warehouse_id ON inventory (warehouse_id);
CREATE INDEX idx_inventory_low_stock ON inventory ((quantity - reserved_quantity)) WHERE (quantity - reserved_quantity) <= reorder_level;

-- Indexes for stock_movements table
CREATE INDEX idx_stock_movements_inventory_id ON stock_movements (inventory_id);
CREATE INDEX idx_stock_movements_reference_id ON stock_movements (reference_id);
CREATE INDEX idx_stock_movements_type ON stock_movements (type);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);

-- Trigger for updated_at
CREATE
OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at
= CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$
language 'plpgsql';

CREATE TRIGGER update_inventory_updated_at
    BEFORE UPDATE
    ON inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
