CREATE TABLE IF NOT EXISTS blog_user (
    email VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255)
);