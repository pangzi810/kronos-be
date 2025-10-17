-- Create users table for testing
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Insert test data
INSERT INTO users (id, username, email, password_hash, role, full_name, is_active, created_at, updated_at) VALUES
('test-user-1', 'testuser1', 'test1@example.com', 'hashedpassword1', 'DEVELOPER', 'Test User 1', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-user-2', 'testuser2', 'test2@example.com', 'hashedpassword2', 'PMO', 'Test User 2', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-user-3', 'testuser3', 'test3@example.com', 'hashedpassword3', 'DEVELOPER', 'Test User 3', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);