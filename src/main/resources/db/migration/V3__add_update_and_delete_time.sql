ALTER TABLE devices ADD COLUMN update_time TIMESTAMP WITH TIME ZONE;
ALTER TABLE devices ADD COLUMN delete_time TIMESTAMP WITH TIME ZONE;

UPDATE devices SET update_time = creation_time WHERE update_time IS NULL;

ALTER TABLE devices ALTER COLUMN update_time SET NOT NULL;
