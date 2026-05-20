-- V1: Shared base schema — users, pharmacies, prescriptions, orders
-- This migration establishes the shared tables used by all workflow teams.

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'ROLE_PATIENT',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pharmacies (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    address   VARCHAR(500),
    latitude  DOUBLE,
    longitude DOUBLE,
    phone     VARCHAR(30),
    email     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id   BIGINT NOT NULL,
    pharmacy_id  BIGINT,
    status       VARCHAR(50) DEFAULT 'PENDING',
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)  REFERENCES users(id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT NOT NULL,
    pharmacy_id   BIGINT,
    total_amount  DECIMAL(10, 2),
    status        VARCHAR(50) DEFAULT 'PENDING',
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)  REFERENCES users(id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id)
);
