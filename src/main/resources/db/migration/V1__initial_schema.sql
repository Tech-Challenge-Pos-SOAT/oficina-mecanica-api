-- ========================================================================
-- Schema: Mechanic Shop Management System (MVP)
-- Database: PostgreSQL
-- =============================== CUSTOMER ===============================
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(18) NOT NULL UNIQUE, -- CPF or CNPJ
    phone VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== VEHICLE ===============================
CREATE TABLE vehicle (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    plate VARCHAR(10) NOT NULL UNIQUE,
    year INT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_vehicle_customer ON vehicle(customer_id);

-- =============================== MATERIAL ===============================
CREATE TABLE material (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    stock_minimum INT NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ===============================
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== EMPLOYEE ===============================
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / INACTIVE
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================== SERVICE ORDER ===============================
CREATE TABLE service_order (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicle(id),
    price NUMERIC(10,2),
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_service_order_customer ON service_order(customer_id);
CREATE INDEX idx_service_order_vehicle ON service_order(vehicle_id);
CREATE INDEX idx_service_order_status ON service_order(status);

-- =============================== SERVICE ORDER - MATERIAL ===============================
CREATE TABLE service_order_material (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    material_id BIGINT NOT NULL REFERENCES material(id),
    quantity INT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_material_order ON service_order_material(service_order_id);

-- =============================== SERVICE ORDER - SERVICE ===============================
CREATE TABLE service_order_service (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    service_id BIGINT NOT NULL REFERENCES service(id),
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_service_order ON service_order_service(service_order_id);

-- =============================== SERVICE ORDER - STATUS HISTORY ===============================
CREATE TABLE service_order_history (
    id BIGSERIAL PRIMARY KEY,
    service_order_id BIGINT NOT NULL REFERENCES service_order(id),
    status VARCHAR(30) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    author_type VARCHAR(20) NOT NULL, -- CUSTOMER / EMPLOYEE / SYSTEM
    author_id BIGINT, -- null when SYSTEM
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_so_history_order ON service_order_history(service_order_id);

-- =============================== MATERIAL TRANSACTION ===============================
CREATE TABLE material_transaction (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES material(id),
    service_order_id BIGINT REFERENCES service_order(id), -- null on stock entry?
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL, -- IN / OUT
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mt_material ON material_transaction(material_id);
CREATE INDEX idx_mt_type ON material_transaction(type);
