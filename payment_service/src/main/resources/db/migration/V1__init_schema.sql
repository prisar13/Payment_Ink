CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- ROLE TABLE
-- =========================
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- =========================
-- APP USER TABLE
-- =========================
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- =========================
-- USER ROLES JOIN TABLE
-- =========================
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- =========================
-- TRANSACTIONS TABLE
-- =========================
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),

    type VARCHAR(50),
    utr VARCHAR(255),
    amount NUMERIC(19, 4),
    description TEXT,
    status VARCHAR(50)
);