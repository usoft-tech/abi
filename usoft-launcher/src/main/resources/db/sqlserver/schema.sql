CREATE TABLE sys_dict_type (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  dict_name NVARCHAR(100) NOT NULL,
  dict_type NVARCHAR(100) NOT NULL UNIQUE,
  status NVARCHAR(32) DEFAULT 'ENABLE',
  remark NVARCHAR(500),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_dict_data (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  dict_sort INT DEFAULT 0,
  dict_label NVARCHAR(100) NOT NULL,
  dict_value NVARCHAR(100) NOT NULL,
  dict_type NVARCHAR(100) NOT NULL,
  css_class NVARCHAR(100),
  list_class NVARCHAR(100),
  is_default NCHAR(1) DEFAULT 'N',
  status NVARCHAR(32) DEFAULT 'ENABLE',
  remark NVARCHAR(500),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_user (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  username NVARCHAR(64) NOT NULL,
  password NVARCHAR(255) NOT NULL,
  department_id NVARCHAR(64),
  employee_no NVARCHAR(64),
  display_name NVARCHAR(128),
  avatar NVARCHAR(512),
  status NVARCHAR(32),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_role (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  name NVARCHAR(64) NOT NULL,
  [code] NVARCHAR(64) NOT NULL,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_department (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  parent_id NVARCHAR(64),
  ancestor_ids NVARCHAR(1024),
  sort INT,
  ancestor_sorts NVARCHAR(1024),
  level INT,
  is_leaf BIT,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_config (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  config_key NVARCHAR(128) NOT NULL,
  config_value NVARCHAR(1024) NOT NULL,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_operation_log (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  username NVARCHAR(64),
  tenant_id NVARCHAR(64),
  module NVARCHAR(128),
  action NVARCHAR(128),
  method NVARCHAR(512),
  uri NVARCHAR(512),
  status NVARCHAR(32),
  duration_ms BIGINT,
  request_body NVARCHAR(MAX),
  response_body NVARCHAR(MAX),
  created_at DATETIME2,
  is_deleted BIT DEFAULT 0,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_tenant (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  code NVARCHAR(64) NOT NULL,
  name NVARCHAR(128) NOT NULL,
  status NVARCHAR(32) NOT NULL,
  cover NVARCHAR(1024),
  description NVARCHAR(MAX),
  site_config NVARCHAR(MAX),
  contact_user NVARCHAR(64),
  contact_phone NVARCHAR(64),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_user_tenant (
  user_id NVARCHAR(64) NOT NULL,
  tenant_id NVARCHAR(64) NOT NULL,
  auth_key NVARCHAR(64),
  CONSTRAINT pk_sys_user_tenant PRIMARY KEY (user_id, tenant_id)
);

CREATE TABLE sys_auth_client (
  tenant_id NVARCHAR(64) NOT NULL,
  client_id NVARCHAR(128) NOT NULL,
  name NVARCHAR(128),
  description NVARCHAR(1000),
  client_secret NVARCHAR(255) NOT NULL,
  expired_at DATETIME2 NULL,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64),
  CONSTRAINT pk_sys_auth_client PRIMARY KEY (tenant_id, client_id)
);

CREATE TABLE bi_datasource (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  type NVARCHAR(32) NOT NULL,
  driver_class_name NVARCHAR(256),
  url NVARCHAR(1024) NOT NULL,
  host NVARCHAR(256),
  port INT,
  database_name NVARCHAR(128),
  username NVARCHAR(256),
  password NVARCHAR(256),
  ext_props NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE ai_model (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  provider NVARCHAR(32) NOT NULL,
  model NVARCHAR(128) NOT NULL,
  base_url NVARCHAR(512),
  api_key NVARCHAR(256),
  ext_props NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_file (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(255) NOT NULL,
  url NVARCHAR(1024) NOT NULL,
  content_type NVARCHAR(128),
  size BIGINT,
  storage_path NVARCHAR(200),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_icon (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  svg NVARCHAR(MAX) NOT NULL,
  description NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_page (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  description NVARCHAR(1000),
  industry NVARCHAR(255),
  schema_json NVARCHAR(MAX),
  status NVARCHAR(32),
  cover NVARCHAR(1024),
  db_ids NVARCHAR(1024),
  excel_ids NVARCHAR(1024),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE ai_conversation (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  biz_type NVARCHAR(64),
  biz_id NVARCHAR(64),
  name NVARCHAR(128),
  tenant_id NVARCHAR(64),
  is_actived BIT DEFAULT 1,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64)
);

CREATE TABLE ai_conversation_message (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  conversation_id NVARCHAR(64) NOT NULL,
  question NVARCHAR(4000),
  prompt NVARCHAR(MAX),
  answer NVARCHAR(MAX),
  files NVARCHAR(MAX),
  agents NVARCHAR(MAX),
  at_items NVARCHAR(MAX),
  extra_props NVARCHAR(MAX),
  tenant_id NVARCHAR(64),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  CONSTRAINT chk_ai_conv_msg_files_json CHECK (ISJSON(files) = 1),
  CONSTRAINT chk_ai_conv_msg_agents_json CHECK (ISJSON(agents) = 1)
);

CREATE TABLE bi_datasource_db (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  datasource_id NVARCHAR(64) NOT NULL,
  name NVARCHAR(128) NOT NULL,
  type NVARCHAR(64),
  dbid NVARCHAR(64),
  description NVARCHAR(MAX),
  status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_datasource_table (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  datasource_id NVARCHAR(64) NOT NULL,
  db_id NVARCHAR(64) NOT NULL,
  name NVARCHAR(128) NOT NULL,
  description NVARCHAR(MAX),
  status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_datasource_field (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  datasource_id NVARCHAR(64) NOT NULL,
  db_id NVARCHAR(64) NOT NULL,
  table_id NVARCHAR(64) NOT NULL,
  name NVARCHAR(128) NOT NULL,
  type NVARCHAR(64),
  description NVARCHAR(MAX),
  status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_app (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  menu_type NVARCHAR(32),
  title NVARCHAR(128) NOT NULL,
  icon NVARCHAR(128),
  page_id NVARCHAR(64),
  app_key NVARCHAR(64),
  redirect_url NVARCHAR(1024),
  parent_id NVARCHAR(64),
  ancestor_ids NVARCHAR(1024),
  sort INT,
  ancestor_sorts NVARCHAR(1024),
  level INT,
  is_leaf BIT,
  has_watermark BIT DEFAULT 0,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_dataset_folder (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  parent_id NVARCHAR(64),
  ancestor_ids NVARCHAR(1024),
  sort INT,
  ancestor_sorts NVARCHAR(1024),
  level INT,
  is_leaf BIT,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_dataset (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  folder_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  type NVARCHAR(32) NOT NULL,
  config NVARCHAR(MAX),
  description NVARCHAR(1000),
  status NVARCHAR(32) NOT NULL DEFAULT 'enabled',
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_permission (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  parent_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  code NVARCHAR(64) NOT NULL,
  type NVARCHAR(32) NOT NULL,
  description NVARCHAR(1000),
  sort INT DEFAULT 0,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_role_permission (
  role_id NVARCHAR(64) NOT NULL,
  permission_id NVARCHAR(64) NOT NULL,
  CONSTRAINT pk_sys_role_permission PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE sys_user_role (
  user_id NVARCHAR(64) NOT NULL,
  role_id NVARCHAR(64) NOT NULL,
  CONSTRAINT pk_sys_user_role PRIMARY KEY (user_id, role_id)
);

CREATE TABLE bi_authorization (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  biz_type NVARCHAR(32) NOT NULL,
  biz_id NVARCHAR(64) NOT NULL,
  authorizer_scope NVARCHAR(32) NOT NULL,
  authorizer_id NVARCHAR(64),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_share (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  biz_type NVARCHAR(32) NOT NULL,
  biz_id NVARCHAR(64) NOT NULL,
  share_key NVARCHAR(64) NOT NULL,
  expire_type NVARCHAR(32) NOT NULL,
  expire_at DATETIME2 NULL,
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_page_report (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  page_id NVARCHAR(64),
  title NVARCHAR(255),
  content NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_page_example (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  description NVARCHAR(1000),
  cover NVARCHAR(1024),
  item_json NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE bi_page_template (
  id NVARCHAR(64) NOT NULL PRIMARY KEY,
  tenant_id NVARCHAR(64),
  name NVARCHAR(128) NOT NULL,
  description NVARCHAR(1000),
  cover NVARCHAR(1024),
  schema_json NVARCHAR(MAX),
  is_deleted BIT DEFAULT 0,
  created_at DATETIME2 NULL,
  created_by NVARCHAR(64),
  updated_at DATETIME2 NULL,
  updated_by NVARCHAR(64)
);

CREATE TABLE sys_third_auth_user (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id NVARCHAR(64) NOT NULL,
  source NVARCHAR(32) NOT NULL,
  uuid NVARCHAR(128) NOT NULL,
  union_id NVARCHAR(128),
  username NVARCHAR(128),
  nickname NVARCHAR(128),
  avatar NVARCHAR(512),
  raw_data NVARCHAR(MAX),
  create_time DATETIME2,
  update_time DATETIME2
);

CREATE INDEX idx_source_uuid ON sys_third_auth_user (source, uuid);

CREATE TABLE agentscope_sessions (
  session_id NVARCHAR(255) NOT NULL,
  state_key NVARCHAR(255) NOT NULL,
  item_index INT NOT NULL DEFAULT 0,
  state_data NVARCHAR(MAX) NOT NULL,
  created_at DATETIME2 DEFAULT GETDATE(),
  updated_at DATETIME2 DEFAULT GETDATE(),
  PRIMARY KEY (session_id, state_key, item_index)
);
