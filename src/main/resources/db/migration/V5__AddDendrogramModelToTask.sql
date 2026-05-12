ALTER TABLE task ADD COLUMN dendrogram_model JSONB NOT NULL DEFAULT '{}'::JSONB;

-- fix data points column to allow varchar of length 2 (to allow up to 99 data points)
ALTER TABLE cluster ALTER COLUMN data_points TYPE VARCHAR(2)[];

