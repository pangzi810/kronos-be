-- Create work_records table for testing
CREATE TABLE IF NOT EXISTS work_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    work_date DATE NOT NULL,
    category_hours TEXT,
    description TEXT,
    total_hours DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL
);

-- Insert test data  
INSERT INTO work_records (id, user_id, project_id, work_date, category_hours, description, total_hours, created_at, updated_at) VALUES
('test-record-1', 'test-user-1', 'test-project-1', '2024-01-15', '{"DEV": 8.0}', 'Development work', 8.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-record-2', 'test-user-2', 'test-project-2', '2024-01-16', '{"MEETING": 2.0, "DEV": 6.0}', 'Meeting and development', 8.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-record-3', 'test-user-1', 'test-project-2', '2024-01-17', '{"REVIEW": 4.0}', 'Code review', 4.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);