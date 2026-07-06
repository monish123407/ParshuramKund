DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    date_of_holi_dip VARCHAR(255) NOT NULL,
    age BIGINT NOT NULL,
    comorbidities VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    present_address VARCHAR(255) NOT NULL,
    present_state VARCHAR(255) NOT NULL,
    booking_date VARCHAR(255) NOT NULL,
    present_district VARCHAR(255) NOT NULL,
    present_pin_code VARCHAR(255) NOT NULL,
    permanent_address VARCHAR(255) NOT NULL,
    permanent_state VARCHAR(255) NOT NULL,
    permanent_district VARCHAR(255) NOT NULL,
    permanent_pin_code VARCHAR(255) NOT NULL,
    co_applicant TEXT,
    is_present_co_applicant BOOLEAN,
    aadhar_number VARCHAR(255),
    aadhar_photo_path VARCHAR(255)
);
