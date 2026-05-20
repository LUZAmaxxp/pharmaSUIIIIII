-- V3: Admin workflow schema
-- Adds the active flag to pharmacies and creates admin-specific tables.

-- Add active flag to shared pharmacies table
ALTER TABLE pharmacies
    ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1;

-- Admin user association table
CREATE TABLE IF NOT EXISTS admin_users (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL UNIQUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Audit trail — every admin action persists a row here via the Observer pattern
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    admin_id    BIGINT       NOT NULL,
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(60),
    target_id   BIGINT,
    detail      TEXT,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed the admin_users record for the seeded admin account (user_id = 1 from V2)
INSERT IGNORE INTO admin_users (user_id) VALUES (1);
