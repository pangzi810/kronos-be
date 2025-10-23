-- Create project_assignments table for testing
CREATE TABLE IF NOT EXISTS project_assignments (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    assigned_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL
);

-- Insert test data
INSERT INTO project_assignments (id, project_id, user_id, role, assigned_at, assigned_by, created_at, updated_at) VALUES
('test-assignment-1', 'test-project-1', 'test-user-1', 'LEAD_DEVELOPER', CURRENT_TIMESTAMP, 'test-admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-assignment-2', 'test-project-1', 'test-user-2', 'DEVELOPER', CURRENT_TIMESTAMP, 'test-admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test-assignment-3', 'test-project-2', 'test-user-1', 'DEVELOPER', CURRENT_TIMESTAMP, 'test-admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);