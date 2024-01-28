CREATE TABLE password_reset (
    reset_token VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    date_created TIMESTAMP NOT NULL
);