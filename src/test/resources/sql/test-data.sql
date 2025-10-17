-- テスト用データ
-- WorkRecordApproval テスト用の初期データ

-- 外部キー制約を一時的に無効化してクリーンアップ
SET FOREIGN_KEY_CHECKS = 0;

-- 既存のデータを完全にクリア（テストの分離を保証）
DELETE FROM work_record_approval;
DELETE FROM work_records WHERE user_id NOT IN (
    'demo-pmo-uuid-0000-000000000001',
    'demo-pmo-uuid-0000-000000000002', 
    'demo-dev-uuid-0000-000000000003',
    'demo-dev-uuid-0000-000000000004',
    'demo-dev-uuid-0000-000000000005'
);
DELETE FROM project_assignments WHERE user_id NOT IN (
    'demo-pmo-uuid-0000-000000000001',
    'demo-pmo-uuid-0000-000000000002', 
    'demo-dev-uuid-0000-000000000003',
    'demo-dev-uuid-0000-000000000004',
    'demo-dev-uuid-0000-000000000005'
);
DELETE FROM users WHERE id NOT IN (
    'demo-pmo-uuid-0000-000000000001',
    'demo-pmo-uuid-0000-000000000002', 
    'demo-dev-uuid-0000-000000000003',
    'demo-dev-uuid-0000-000000000004',
    'demo-dev-uuid-0000-000000000005'
);

-- 外部キー制約を再有効化
SET FOREIGN_KEY_CHECKS = 1;

-- テスト用ユーザーを挿入
INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('test-user-001', 'testuser001', 'test-user-001@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'テストユーザー001', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('test-user-002', 'testuser002', 'test-user-002@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'テストユーザー002', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('test-user-003', 'testuser003', 'test-user-003@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'テストユーザー003', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('supervisor-001', 'supervisor001', 'supervisor-001@example.com', '$2a$10$dummy.hash.for.testing', 'PMO', '上長001', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

-- テストコードで使用されるユーザーIDを追加
INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('user-001', 'user001', 'user-001@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'ユーザー001', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('user-002', 'user002', 'user-002@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'ユーザー002', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

INSERT INTO users (id, username, email, password_hash, role, full_name, created_at, updated_at) VALUES
('user-003', 'user003', 'user-003@example.com', '$2a$10$dummy.hash.for.testing', 'DEVELOPER', 'ユーザー003', '2025-01-14 10:00:00', '2025-01-14 10:00:00');

-- テスト用の初期承認レコードは、各テストメソッドで動的に作成される