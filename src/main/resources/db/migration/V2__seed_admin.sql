-- V2: Seed initial admin user (password = Admin@1234 BCrypt hashed)
-- BCrypt hash of 'Admin@1234' with strength 12
INSERT INTO users (email, password, role)
VALUES ('admin@pharmacy.ma',
        '$2a$12$RtCCUFQwGOLlIFj.lRF5SuyGUuqInzFqYBHSPbUxfT.bvM4WPovRa',
        'ROLE_ADMIN');
