-- Service name must be unique across the catalog
ALTER TABLE service ADD CONSTRAINT uq_service_name UNIQUE (name);
