ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN phone VARCHAR(255);
ALTER TABLE users ADD COLUMN last_updated TIMESTAMP NULL;
ALTER TABLE ous ADD COLUMN last_updated TIMESTAMP NULL;
