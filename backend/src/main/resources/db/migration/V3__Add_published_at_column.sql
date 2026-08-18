-- V3__Add_published_at_column.sql

ALTER TABLE tuition_offers ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

UPDATE tuition_offers 
SET published_at = CASE 
    WHEN posted_at ~ '^\d{4}-\d{2}-\d{2}' THEN posted_at::TIMESTAMP 
    ELSE scraped_at 
END
WHERE published_at IS NULL;
