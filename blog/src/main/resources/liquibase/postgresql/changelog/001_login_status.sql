CREATE TABLE IF NOT EXISTS login_status (
    token VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login_date TIMESTAMP NOT NULL,
    token_expiration_date TIMESTAMP NOT NULL,
    logout_date TIMESTAMP
);

CREATE INDEX email_index ON login_status (email);