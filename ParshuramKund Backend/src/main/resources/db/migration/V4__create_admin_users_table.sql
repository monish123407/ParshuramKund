CREATE TABLE IF NOT EXISTS admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL
);

INSERT INTO admin_users (username, password, full_name, role)
VALUES ('admin', 'admin123', 'System Administrator', 'SUPER_ADMIN')
ON CONFLICT (username) DO NOTHING;
