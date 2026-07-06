CREATE TABLE IF NOT EXISTS portal_roles (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon VARCHAR(50) NOT NULL,
    color VARCHAR(20) NOT NULL
);

INSERT INTO portal_roles (role_code, title, description, icon, color) VALUES
('SUPER_ADMIN', 'Mela Administrator', 'Full administrative access to manage operators, view logs, and audit entries.', 'admin_panel_settings', '#d97706'),
('MELA_OPERATOR', 'Mela Operator', 'Daily operational access to scan barcodes, verify passes, and manage check-ins.', 'qr_code_scanner', '#2563eb'),
('AUDITOR', 'Auditor', 'Read-only access to view registration history, stats, and generate audit reports.', 'assessment', '#059669')
ON CONFLICT (role_code) DO NOTHING;
