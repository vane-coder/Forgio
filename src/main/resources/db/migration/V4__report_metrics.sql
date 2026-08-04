-- Persist computed report metrics so listing past reports shows real numbers
-- (previously getAllReports returned hardcoded zeros because these weren't stored).
ALTER TABLE reports ADD COLUMN production_entries INT;
ALTER TABLE reports ADD COLUMN total_produced NUMERIC(12,3);
ALTER TABLE reports ADD COLUMN low_stock_materials INT;
ALTER TABLE reports ADD COLUMN machines_stopped INT;
