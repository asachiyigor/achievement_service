CREATE TABLE IF NOT EXISTS users
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    username   VARCHAR(128) NOT NULL UNIQUE,
    password   VARCHAR(128) NOT NULL,
    created_at timestamptz DEFAULT current_timestamp,
    updated_at timestamptz DEFAULT current_timestamp
);