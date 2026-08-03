-- Goods-sold: workers record a sale of finished goods / surplus material.
-- material_id is optional (a sale may reference stock or be a free-typed item).
CREATE TABLE sales (
    sale_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id  UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    sold_by     UUID NOT NULL REFERENCES users(user_id),
    material_id UUID REFERENCES raw_materials(material_id),
    item_name   VARCHAR(255) NOT NULL,
    quantity    DECIMAL(12,3) NOT NULL,
    unit        VARCHAR(50),
    unit_price  DECIMAL(12,2) NOT NULL,
    total       DECIMAL(14,2) NOT NULL,
    sold_to     VARCHAR(255),
    notes       TEXT,
    sold_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sales_factory ON sales(factory_id);
