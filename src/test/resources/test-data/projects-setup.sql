-- Create projects table for testing
CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    planned_end_date DATE NOT NULL,
    actual_end_date DATE NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL
);

-- Insert test data
INSERT INTO projects (id, name, description, status, start_date, planned_end_date, created_by, created_at, updated_at) VALUES
('test-project-1', 'Test Project 1', 'First test project', 'PLANNING', '2024-01-01', '2024-03-01', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-project-2', 'Test Project 2', 'Second test project', 'IN_PROGRESS', '2024-02-01', '2024-04-01', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-project-3', 'Test Project 3', 'Third test project', 'COMPLETED', '2024-01-15', '2024-02-15', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);