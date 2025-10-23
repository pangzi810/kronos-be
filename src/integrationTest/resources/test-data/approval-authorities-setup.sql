-- Test data for ApprovalAuthorityMapper tests
-- 複数の承認権限者のテストデータを準備

INSERT INTO approval_authorities (
    id, email, name, position,
    level_1_code, level_1_name,
    level_2_code, level_2_name,
    level_3_code, level_3_name,
    level_4_code, level_4_name,
    created_at, updated_at
) VALUES
-- 最初のレコード（最古の作成日時）
('test-id-1', 'testuser1@example.com', '山田太郎', 'マネージャー',
 'L1001', '開発本部', 'L2001', 'システム開発部', 'L3001', 'Webアプリ課', NULL, NULL,
 '2024-01-15 09:00:00', '2024-01-15 09:00:00'),

-- 2番目のレコード
('test-id-2', 'testuser2@example.com', '佐藤花子', '部長',
 'L1001', '開発本部', 'L2002', 'インフラ部', NULL, NULL, NULL, NULL,
 '2024-01-15 10:00:00', '2024-01-15 10:00:00'),

-- 3番目のレコード（最新の作成日時）
('test-id-3', 'testuser3@example.com', '田中次郎', '本部長',
 'L1002', '営業本部', NULL, NULL, NULL, NULL, NULL, NULL,
 '2024-01-15 11:00:00', '2024-01-15 11:00:00');