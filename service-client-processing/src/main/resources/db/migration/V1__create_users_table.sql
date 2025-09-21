CREATE TABLE IF NOT EXISTS client_processing.users
(
    id       BIGSERIAL PRIMARY KEY,
    login    VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE
);