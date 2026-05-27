CREATE TABLE IF NOT EXISTS sys_dict_type (
  id VARCHAR(64) PRIMARY KEY,
  dict_name VARCHAR(100) NOT NULL,
  dict_type VARCHAR(100) NOT NULL,
  status VARCHAR(32) DEFAULT 'ENABLE',
  remark VARCHAR(500),
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64),
  UNIQUE(dict_type)
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
  id VARCHAR(64) PRIMARY KEY,
  dict_sort INT DEFAULT 0,
  dict_label VARCHAR(100) NOT NULL,
  dict_value VARCHAR(100) NOT NULL,
  dict_type VARCHAR(100) NOT NULL,
  css_class VARCHAR(100),
  list_class VARCHAR(100),
  is_default CHAR(1) DEFAULT 'N',
  status VARCHAR(32) DEFAULT 'ENABLE',
  remark VARCHAR(500),
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_user (
  id VARCHAR(64) PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  department_id VARCHAR(64),
  employee_no VARCHAR(64)
  , display_name VARCHAR(128)
  , avatar VARCHAR(512)
  , status VARCHAR(32)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_role (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_department (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  parent_id VARCHAR(64),
  ancestor_ids VARCHAR(1024),
  sort INT,
  ancestor_sorts VARCHAR(1024),
  level INT,
  is_leaf TINYINT(1)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_config (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  config_key VARCHAR(128) NOT NULL,
  config_value VARCHAR(1024) NOT NULL
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64),
  tenant_id VARCHAR(64),
  module VARCHAR(128),
  action VARCHAR(128),
  method VARCHAR(512),
  uri VARCHAR(512),
  status VARCHAR(32),
  duration_ms BIGINT,
  request_body LONGTEXT,
  response_body LONGTEXT,
  created_at TIMESTAMP(6)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_tenant (
  id VARCHAR(64) PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL
  , cover VARCHAR(1024)
  , description LONGTEXT
  , site_config LONGTEXT
  , contact_user VARCHAR(64)
  , contact_phone VARCHAR(64)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_user_tenant (
  user_id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  auth_key VARCHAR(64),
  PRIMARY KEY (user_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS sys_auth_client (
  tenant_id VARCHAR(64) NOT NULL,
  client_id VARCHAR(128) NOT NULL,
  name VARCHAR(128),
  description VARCHAR(1000),
  client_secret VARCHAR(255) NOT NULL,
  expired_at TIMESTAMP NULL
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
  , PRIMARY KEY (tenant_id, client_id)
);

CREATE TABLE IF NOT EXISTS bi_datasource (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  type VARCHAR(32) NOT NULL,
  driver_class_name VARCHAR(256),
  url VARCHAR(1024) NOT NULL,
  host VARCHAR(256),
  port INT,
  database_name VARCHAR(128),
  username VARCHAR(256),
  password VARCHAR(256),
  ext_props LONGTEXT
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS ai_model (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model VARCHAR(128) NOT NULL,
  base_url VARCHAR(512),
  api_key VARCHAR(256),
  ext_props LONGTEXT
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_file (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(255) NOT NULL,
  url VARCHAR(1024) NOT NULL,
  content_type VARCHAR(128),
  size BIGINT,
  storage_path VARCHAR(200)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_icon (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  svg LONGTEXT NOT NULL,
  description LONGTEXT
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_page (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  description VARCHAR(1000),
  industry VARCHAR(255),
  schema_json LONGTEXT,
  status VARCHAR(32),
  cover VARCHAR(1024),
  db_ids VARCHAR(1024),
  excel_ids VARCHAR(1024)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS ai_conversation (
  id VARCHAR(64) PRIMARY KEY,
  biz_type VARCHAR(64),
  biz_id VARCHAR(64),
  name VARCHAR(128),
  tenant_id VARCHAR(64),
  is_actived TINYINT(1) DEFAULT 1
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS ai_conversation_message (
  id VARCHAR(64) PRIMARY KEY,
  conversation_id VARCHAR(64) NOT NULL,
  question VARCHAR(4000),
  prompt LONGTEXT,
  answer LONGTEXT,
  files JSON,
  agents JSON,
  at_items LONGTEXT,
  extra_props LONGTEXT,
  tenant_id VARCHAR(64)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_datasource_db (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  datasource_id VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  type VARCHAR(64),
  dbid VARCHAR(64),
  description LONGTEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'enabled'
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_datasource_table (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  datasource_id VARCHAR(64) NOT NULL,
  db_id VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description LONGTEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'enabled'
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_datasource_field (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  datasource_id VARCHAR(64) NOT NULL,
  db_id VARCHAR(64) NOT NULL,
  table_id VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  type VARCHAR(64),
  description LONGTEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'enabled'
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_app (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  menu_type VARCHAR(32),
  title VARCHAR(128) NOT NULL,
  icon VARCHAR(128),
  page_id VARCHAR(64),
  app_key VARCHAR(64),
  redirect_url VARCHAR(1024),
  parent_id VARCHAR(64),
  ancestor_ids VARCHAR(1024),
  sort INT DEFAULT 0,
  ancestor_sorts VARCHAR(1024),
  level INT,
  is_leaf TINYINT(1),
  has_watermark TINYINT(1) DEFAULT 0
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_dataset_folder (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  parent_id VARCHAR(64),
  ancestor_ids VARCHAR(1024),
  sort INT DEFAULT 0,
  ancestor_sorts VARCHAR(1024),
  level INT,
  is_leaf TINYINT(1)
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_dataset (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  folder_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  type VARCHAR(32) NOT NULL,
  config LONGTEXT,
  description VARCHAR(1000),
  status VARCHAR(32) NOT NULL DEFAULT 'enabled'
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_permission (
  id VARCHAR(64) PRIMARY KEY,
  parent_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  code VARCHAR(64) NOT NULL,
  type VARCHAR(32) NOT NULL,
  description VARCHAR(1000),
  sort INT DEFAULT 0
  , is_deleted TINYINT(1) DEFAULT 0
  , created_at TIMESTAMP NULL
  , created_by VARCHAR(64)
  , updated_at TIMESTAMP NULL
  , updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id VARCHAR(64) NOT NULL,
  permission_id VARCHAR(64) NOT NULL,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id VARCHAR(64) NOT NULL,
  role_id VARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS bi_authorization (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  biz_type VARCHAR(32) NOT NULL,
  biz_id VARCHAR(64) NOT NULL,
  authorizer_scope VARCHAR(32) NOT NULL,
  authorizer_id VARCHAR(64),
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_share (
  id VARCHAR(64) PRIMARY KEY,
  biz_type VARCHAR(32) NOT NULL,
  biz_id VARCHAR(64) NOT NULL,
  share_key VARCHAR(64) NOT NULL,
  expire_type VARCHAR(32) NOT NULL,
  expire_at TIMESTAMP NULL,
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_page_report (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  page_id VARCHAR(64),
  title VARCHAR(255),
  content LONGTEXT,
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_page_example (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  description VARCHAR(1000),
  cover VARCHAR(1024),
  item_json LONGTEXT,
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS bi_page_template (
  id VARCHAR(64) PRIMARY KEY,
  tenant_id VARCHAR(64),
  name VARCHAR(128) NOT NULL,
  description VARCHAR(1000),
  cover VARCHAR(1024),
  schema_json LONGTEXT,
  is_deleted TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP NULL,
  created_by VARCHAR(64),
  updated_at TIMESTAMP NULL,
  updated_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS sys_third_auth_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  source VARCHAR(32) NOT NULL,
  uuid VARCHAR(128) NOT NULL,
  union_id VARCHAR(128),
  username VARCHAR(128),
  nickname VARCHAR(128),
  avatar VARCHAR(512),
  raw_data LONGTEXT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sys_third_auth_user_source_uuid ON sys_third_auth_user(source, uuid);

CREATE TABLE IF NOT EXISTS agentscope_sessions (
  session_id varchar(255) NOT NULL,
  state_key varchar(255) NOT NULL,
  item_index int NOT NULL DEFAULT 0,
  state_data LONGTEXT NOT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (session_id,state_key,item_index)
);
