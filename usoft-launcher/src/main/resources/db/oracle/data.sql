INSERT INTO sys_user (id, username, password, is_deleted, created_at, created_by) VALUES ('1', 'admin', '$2a$10$KJ9YhYhJQYk4M.9MwfV9xOHyMFts86TCXxFttkmfS0YU3U5ZRJCwi', 0, CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_tenant (id, code, name, status, cover, description, contact_user, contact_phone, is_deleted, created_at, created_by) VALUES ('1', 'system', 'System Tenant', 'ENABLE', NULL, NULL, NULL, NULL, 0, CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_user_tenant (user_id, tenant_id, auth_key) VALUES ('1', '1', 'ADMIN');
