INSERT INTO sys_user (id, username, password, is_deleted, created_at, created_by) VALUES ('1', 'admin', '$2a$10$KJ9YhYhJQYk4M.9MwfV9xOHyMFts86TCXxFttkmfS0YU3U5ZRJCwi', 0, CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, created_at, created_by) VALUES ('1', '用户性别', 'sys_user_sex', 'ENABLE', '用户性别列表', CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('1', 1, '男', '0', 'sys_user_sex', 'Y', 'ENABLE', CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('2', 2, '女', '1', 'sys_user_sex', 'N', 'ENABLE', CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('3', 3, '未知', '2', 'sys_user_sex', 'N', 'ENABLE', CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_tenant (id, code, name, status, is_deleted, created_at, created_by) VALUES ('1', 'system', 'System Tenant', 'ENABLE', 0, CURRENT_TIMESTAMP, 'system');
INSERT INTO sys_user_tenant (user_id, tenant_id, auth_key) VALUES ('1', '1', 'ADMIN');
