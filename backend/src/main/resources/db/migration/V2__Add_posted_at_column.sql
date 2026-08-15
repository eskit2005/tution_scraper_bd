-- V2__Add_posted_at_column.sql

ALTER TABLE tuition_offers ADD COLUMN IF NOT EXISTS posted_at VARCHAR(255);
