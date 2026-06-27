CREATE TABLE IF NOT EXISTS inquiries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    submitted_at VARCHAR(255) NOT NULL
);
