-- ── Shipment cargo: tie line items to raw materials ─────────────
-- Cargo is now chosen from a factory's materials list (materialId + quantity)
-- rather than a free-text description. Add the material FK and relax the old
-- NOT NULL description so material-based rows stay valid.
ALTER TABLE shipment_items ADD COLUMN material_id UUID REFERENCES raw_materials(material_id);
ALTER TABLE shipment_items ALTER COLUMN description DROP NOT NULL;
