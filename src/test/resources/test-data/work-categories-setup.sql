-- Create work_categories table for testing
CREATE TABLE IF NOT EXISTS work_categories (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP NULL
);

-- Insert test data
INSERT INTO work_categories (id, code, name, description, is_active, display_order, created_by, created_at, updated_by, updated_at) VALUES
('test-category-1', 'DEV', '開発作業', '開発・実装作業', TRUE, 1, 'test-user', CURRENT_TIMESTAMP, 'test-user', CURRENT_TIMESTAMP),
('test-category-2', 'MEETING', '会議', '各種会議・打ち合わせ', TRUE, 2, 'test-user', CURRENT_TIMESTAMP, 'test-user', CURRENT_TIMESTAMP),
('test-category-3', 'REVIEW', 'レビュー', 'コードレビュー・設計レビュー', TRUE, 3, 'test-user', CURRENT_TIMESTAMP, 'test-user', CURRENT_TIMESTAMP),
('test-category-4', 'TESTING', 'テスト', 'テスト作成・実行', FALSE, 4, 'test-user', CURRENT_TIMESTAMP, 'test-user', CURRENT_TIMESTAMP);