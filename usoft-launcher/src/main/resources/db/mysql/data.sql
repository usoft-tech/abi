INSERT INTO sys_user (id, username, display_name, password, is_deleted, created_at, created_by) VALUES ('1', 'admin', '管理员', '$2a$10$KJ9YhYhJQYk4M.9MwfV9xOHyMFts86TCXxFttkmfS0YU3U5ZRJCwi', 0, NOW(), 'system');
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, created_at, created_by) VALUES ('1', '用户性别', 'sys_user_sex', 'ENABLE', '用户性别列表', NOW(), 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('1', 1, '男', '0', 'sys_user_sex', 'Y', 'ENABLE', NOW(), 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('2', 2, '女', '1', 'sys_user_sex', 'N', 'ENABLE', NOW(), 'system');
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, created_at, created_by) VALUES ('3', 3, '未知', '2', 'sys_user_sex', 'N', 'ENABLE', NOW(), 'system');
INSERT INTO sys_tenant (id, code, name, status, cover, description, contact_user, contact_phone, is_deleted, created_at, created_by) VALUES ('1', 'system', '默认租户', 'ENABLE', NULL, NULL, NULL, NULL, 0, NOW(), 'system');
INSERT INTO sys_user_tenant (user_id, tenant_id, auth_key) VALUES ('1', '1', 'ADMIN');
