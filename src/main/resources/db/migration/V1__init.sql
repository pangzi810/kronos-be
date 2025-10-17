-- システム関連テーブル
CREATE TABLE shedlock (
  name varchar(64) NOT NULL,
  lock_until timestamp NOT NULL,
  locked_at timestamp NOT NULL,
  locked_by varchar(255) NOT NULL,
  PRIMARY KEY (name),
  KEY idx_shedlock_lock_until (lock_until)
);

CREATE TABLE domain_events (
  event_id varchar(100) ,
  aggregate_id varchar(36) ,
  aggregate_type varchar(50) ,
  event_type varchar(100) ,
  event_action varchar(255) ,
  event_data json NOT NULL ,
  event_status varchar(20) ,
  partition_key varchar(36) ,
  occurred_at timestamp NOT NULL ,
  published_at timestamp NULL DEFAULT NULL ,
  retry_count int NOT NULL DEFAULT '0' ,
  error_message text ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (event_id),
  KEY idx_domain_events_status (event_status),
  KEY idx_domain_events_aggregate_id (aggregate_id),
  KEY idx_domain_events_occurred_at (occurred_at),
  KEY idx_domain_events_status_created (event_status,created_at),
  KEY idx_domain_events_retry (event_status,retry_count)
);

-- ユーザー・プロジェクト管理関連テーブル
CREATE TABLE users (
  id varchar(36) ,
  username varchar(50) ,
  email varchar(255) ,
  full_name varchar(255) ,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  deleted_at timestamp NULL DEFAULT NULL ,
  okta_user_id varchar(255) ,
  user_status varchar(20) ,
  last_login_at datetime DEFAULT NULL ,
  PRIMARY KEY (id),
  UNIQUE KEY email (email),
  UNIQUE KEY uk_users_okta_user_id (okta_user_id),
  UNIQUE KEY uk_users_email_deleted_at (email,deleted_at)
);

CREATE TABLE projects (
  id varchar(36) ,
  name varchar(255) ,
  description text ,
  start_date date NOT NULL ,
  planned_end_date date NOT NULL ,
  actual_end_date date DEFAULT NULL ,
  status varchar(20) ,
  created_by varchar(36) ,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  deleted_at timestamp NULL DEFAULT NULL ,
  jira_issue_key varchar(100) ,
  custom_fields TEXT,
  PRIMARY KEY (id),
  UNIQUE KEY idx_projects_jira_issue_key (jira_issue_key),
  KEY idx_projects_name (name),
  CONSTRAINT chk_projects_actual_end CHECK (((actual_end_date is null) or (actual_end_date >= start_date))),
  CONSTRAINT chk_projects_status CHECK ((status in ('PLANNING','IN_PROGRESS','COMPLETED','CANCELLED')))
);

-- JIRA連携関連テーブル
CREATE TABLE jira_response_template (
  id varchar(36) ,
  template_name varchar(255) ,
  velocity_template text ,
  template_description text ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (id),
  UNIQUE KEY idx_response_template_name (template_name)
);

CREATE TABLE jira_jql_queries (
  id varchar(36) ,
  query_name varchar(255) ,
  jql_expression text ,
  template_id varchar(36) ,
  is_active tinyint(1) NOT NULL DEFAULT '1' ,
  priority int NOT NULL DEFAULT '0' ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  created_by varchar(100) ,
  updated_by varchar(100) ,
  PRIMARY KEY (id),
  KEY idx_jql_queries_active_priority (is_active,priority),
  KEY idx_jql_queries_template_id (template_id),
  KEY idx_jql_queries_name (query_name),
  CONSTRAINT fk_jira_jql_queries_template_id FOREIGN KEY (template_id) REFERENCES jira_response_template (id) ON DELETE CASCADE
);

CREATE TABLE jira_sync_histories (
  id varchar(36) ,
  sync_type varchar(50) ,
  sync_status varchar(50) ,
  started_at timestamp NOT NULL ,
  completed_at timestamp NULL DEFAULT NULL ,
  total_projects_processed int NOT NULL DEFAULT '0' ,
  success_count int NOT NULL DEFAULT '0' ,
  error_count int NOT NULL DEFAULT '0' ,
  error_details text ,
  triggered_by varchar(100) ,
  PRIMARY KEY (id),
  KEY idx_sync_histories_status_date (sync_status,started_at),
  KEY idx_sync_histories_type_date (sync_type,started_at),
  KEY idx_sync_histories_triggered_by (triggered_by),
  CONSTRAINT chk_sync_histories_dates CHECK (((completed_at is null) or (completed_at >= started_at))),
  CONSTRAINT chk_sync_histories_sync_status CHECK ((sync_status in ('IN_PROGRESS','COMPLETED','FAILED'))),
  CONSTRAINT chk_sync_histories_sync_type CHECK ((sync_type in ('MANUAL','SCHEDULED')))
);

CREATE TABLE jira_sync_history_details (
  id varchar(36) ,
  sync_history_id varchar(36) ,
  seq int NOT NULL ,
  operation varchar(255) ,
  status varchar(50) ,
  result text ,
  processed_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id),
  KEY idx_sync_history_details_history_id (sync_history_id),
  KEY idx_sync_history_details_status (status),
  CONSTRAINT fk_jira_sync_history_details_history_id FOREIGN KEY (sync_history_id) REFERENCES jira_sync_histories (id) ON DELETE CASCADE,
  CONSTRAINT chk_sync_history_details_status CHECK ((status in ('SUCCESS','ERROR')))
);

-- 作業カテゴリ・工数記録関連テーブル
CREATE TABLE work_categories (
  id varchar(36) ,
  code varchar(20) ,
  name varchar(50) ,
  description text ,
  display_order int NOT NULL ,
  color_code varchar(7) ,
  is_active tinyint(1) DEFAULT '1' ,
  created_by varchar(36) ,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_by varchar(36) ,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  deleted_at timestamp NULL DEFAULT NULL ,
  PRIMARY KEY (id),
  UNIQUE KEY code (code)
);

CREATE TABLE work_records (
  id varchar(36) ,
  user_id varchar(36) ,
  project_id varchar(36) ,
  work_date date NOT NULL ,
  category_hours json NOT NULL ,
  description text ,
  created_by varchar(36) ,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_by varchar(36) ,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  deleted_at timestamp NULL DEFAULT NULL ,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_project_date (user_id,project_id,work_date),
  KEY idx_work_records_user_date (user_id,work_date),
  CONSTRAINT work_records_ibfk_1 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT work_records_ibfk_2 FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

-- 承認機能関連テーブル
CREATE TABLE approval_authorities (
  id varchar(36) ,
  email varchar(255) ,
  name varchar(255) ,
  position varchar(50) ,
  level_1_code varchar(50) ,
  level_1_name varchar(255) ,
  level_2_code varchar(50) ,
  level_2_name varchar(255) ,
  level_3_code varchar(50) ,
  level_3_name varchar(255) ,
  level_4_code varchar(50) ,
  level_4_name varchar(255) ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (id),
  UNIQUE KEY uk_approval_authorities_email (email)
);

CREATE TABLE approvers (
  id varchar(36) ,
  target_email varchar(255) ,
  approver_email varchar(255) ,
  effective_from datetime NOT NULL ,
  effective_to date DEFAULT NULL ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  created_by varchar(36) ,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  updated_by varchar(36) ,
  is_deleted tinyint(1) NOT NULL DEFAULT '0' ,
  PRIMARY KEY (id),
  KEY idx_approvers_target_email (target_email),
  KEY idx_approvers_approver_email (approver_email),
  CONSTRAINT chk_not_self_approver_email CHECK ((target_email <> approver_email))
);

CREATE TABLE work_record_approval (
  user_id varchar(255) ,
  work_date date NOT NULL ,
  approval_status varchar(20) ,
  approver_id varchar(255) ,
  approved_at timestamp NULL DEFAULT NULL ,
  rejection_reason text ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  PRIMARY KEY (user_id,work_date),
  KEY idx_approval_status (approval_status),
  KEY idx_approver_id (approver_id),
  KEY idx_user_status (user_id,approval_status),
  CONSTRAINT fk_wra_approver FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT fk_wra_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_approval_status CHECK ((approval_status in ('PENDING','APPROVED','REJECTED')))
);

CREATE TABLE work_record_approval_histories (
  history_id varchar(100) ,
  work_record_id varchar(36) ,
  user_id varchar(36) ,
  project_id varchar(36) ,
  work_date date NOT NULL ,
  category_hours json NOT NULL ,
  total_hours decimal(4,1) NOT NULL ,
  description text ,
  action varchar(20) ,
  previous_status varchar(20) ,
  current_status varchar(20) ,
  approver_id varchar(36) ,
  rejection_reason text ,
  work_record_snapshot json NOT NULL ,
  occurred_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (history_id),
  KEY idx_approval_histories_work_record_id (work_record_id),
  KEY idx_approval_histories_user_id (user_id),
  KEY idx_approval_histories_approver_id (approver_id),
  KEY idx_approval_histories_action (action),
  KEY idx_approval_histories_occurred_at (occurred_at),
  KEY idx_approval_histories_project_occurred (project_id,occurred_at),
  CONSTRAINT fk_work_record_approval_histories_approver_id FOREIGN KEY (approver_id) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT fk_work_record_approval_histories_project_id FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
  CONSTRAINT fk_work_record_approval_histories_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_work_record_approval_histories_work_record_id FOREIGN KEY (work_record_id) REFERENCES work_records (id) ON DELETE CASCADE
);

-- 作業カテゴリマスターデータの投入
INSERT INTO work_categories (id, code, name, description, display_order, color_code, is_active, created_by, created_at, updated_by, updated_at) VALUES
('category-uuid-brd-000000000001', 'BRD', 'BRD', 'Business requirements document creation/review work', 1, '#FF5722', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-prd-000000000002', 'PRD', 'PRD', 'Product requirements document creation/review work', 2, '#2196F3', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-arc-000000000003', 'ARCHITECTURE', 'Arch', 'System architecture design/review work', 3, '#9C27B0', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-dev-000000000004', 'DEV', 'Dev', 'Programming, implementation, and unit testing work', 4, '#4CAF50', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-tst-000000000004', 'TEST', 'Test', 'Testing work', 4, '#4CAF50', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-ops-000000000005', 'OPERATION', 'Ope', 'System operation, maintenance, and monitoring work', 5, '#FF9800', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-mtg-000000000006', 'MEETING', 'Meeting', 'Project meetings, discussions, and reporting', 6, '#607D8B', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP),
('category-uuid-oth-000000000007', 'OTHERS', 'Others', 'Other work, miscellaneous tasks, and learning time', 7, '#795548', TRUE, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', CURRENT_TIMESTAMP);

INSERT INTO jira_response_template (id, template_name, velocity_template, template_description, created_at, updated_at) VALUES 
('template-standard-001', 'Standard Project Template', 
'{
  "summary": "$!{summary}"
}', 
'標準的なJIRAプロジェクト用のVelocityテンプレート', 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO jira_jql_queries (id, query_name, jql_expression, template_id, is_active, priority, created_at, updated_at, created_by, updated_by) VALUES 
('jql-query-001', 'KAN Epics', 'project = KAN AND issueType = Epic', 'template-standard-001', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin-user-uuid-0000-000000000001', 'admin-user-uuid-0000-000000000001');