-- Initial schema for IntelliSpend

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_roles (
    user_id INTEGER REFERENCES users(id),
    role_id INTEGER REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE expenses (
    id SERIAL PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    payment_method VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'USD',
    is_deleted BOOLEAN DEFAULT FALSE,
    user_id INTEGER REFERENCES users(id) NOT NULL
);

-- Seed roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
