BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_dict_type (
    id VARCHAR2(64) PRIMARY KEY,
    dict_name VARCHAR2(100) NOT NULL,
    dict_type VARCHAR2(100) NOT NULL,
    status VARCHAR2(32) DEFAULT ''ENABLE'',
    remark VARCHAR2(500),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64),
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_dict_data (
    id VARCHAR2(64) PRIMARY KEY,
    dict_sort NUMBER(10) DEFAULT 0,
    dict_label VARCHAR2(100) NOT NULL,
    dict_value VARCHAR2(100) NOT NULL,
    dict_type VARCHAR2(100) NOT NULL,
    css_class VARCHAR2(100),
    list_class VARCHAR2(100),
    is_default CHAR(1) DEFAULT ''N'',
    status VARCHAR2(32) DEFAULT ''ENABLE'',
    remark VARCHAR2(500),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_user (
    id VARCHAR2(64) PRIMARY KEY,
    username VARCHAR2(64) NOT NULL,
    password VARCHAR2(255) NOT NULL,
    department_id VARCHAR2(64),
    employee_no VARCHAR2(64)
  , display_name VARCHAR2(128)
  , avatar VARCHAR2(512)
  , status VARCHAR2(32)
  , is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_role (
    id VARCHAR2(64) PRIMARY KEY,
    name VARCHAR2(64) NOT NULL,
    code VARCHAR2(64) NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_department (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    parent_id VARCHAR2(64),
    ancestor_ids VARCHAR2(1024),
    sort NUMBER(10),
    ancestor_sorts VARCHAR2(1024),
    level NUMBER(10),
    is_leaf NUMBER(1),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_user_tenant (
    user_id VARCHAR2(64) NOT NULL,
    tenant_id VARCHAR2(64) NOT NULL,
    auth_key VARCHAR2(64),
    PRIMARY KEY (user_id, tenant_id)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_auth_client (
    tenant_id VARCHAR2(64) NOT NULL,
    client_id VARCHAR2(128) NOT NULL,
    name VARCHAR2(128),
    description VARCHAR2(1000),
    client_secret VARCHAR2(255) NOT NULL,
    expired_at TIMESTAMP(6) NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64),
    PRIMARY KEY (tenant_id, client_id)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_config (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    config_key VARCHAR2(128) NOT NULL,
    config_value VARCHAR2(1024) NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_operation_log (
    id NUMBER(19) PRIMARY KEY,
    username VARCHAR2(64),
    tenant_id VARCHAR2(64),
    module VARCHAR2(128),
    action VARCHAR2(128),
    method VARCHAR2(512),
    uri VARCHAR2(512),
    status VARCHAR2(32),
    duration_ms NUMBER(19),
    request_body CLOB,
    response_body CLOB,
    created_at TIMESTAMP(6),
    is_deleted NUMBER(1) DEFAULT 0,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE SEQUENCE seq_sys_operation_log START WITH 1 INCREMENT BY 1';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_tenant (
    id VARCHAR2(64) PRIMARY KEY,
    code VARCHAR2(64) NOT NULL,
    name VARCHAR2(128) NOT NULL,
    status VARCHAR2(32) NOT NULL,
    cover VARCHAR2(1024),
    description CLOB,
    site_config CLOB,
    contact_user VARCHAR2(64),
    contact_phone VARCHAR2(64),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_datasource (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    driver_class_name VARCHAR2(256),
    url VARCHAR2(1024) NOT NULL,
    host VARCHAR2(256),
    port NUMBER(10),
    database_name VARCHAR2(128),
    username VARCHAR2(256),
    password VARCHAR2(256),
    ext_props CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE ai_model (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    provider VARCHAR2(32) NOT NULL,
    model VARCHAR2(128) NOT NULL,
    base_url VARCHAR2(512),
    api_key VARCHAR2(256),
    ext_props CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_file (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(255) NOT NULL,
    url VARCHAR2(1024) NOT NULL,
    storage_path VARCHAR2(200),
    content_type VARCHAR2(128),
    size NUMBER(19),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_icon (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    svg CLOB NOT NULL,
    description CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_page (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    description VARCHAR2(1000),
    industry VARCHAR2(255),
    schema_json CLOB,
    status VARCHAR2(32),
    cover VARCHAR2(1024),
    db_ids VARCHAR2(1024),
    excel_ids VARCHAR2(1024)
    , is_deleted NUMBER(1) DEFAULT 0
    , created_at TIMESTAMP(6) NULL
    , created_by VARCHAR2(64)
    , updated_at TIMESTAMP(6) NULL
    , updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE ai_conversation (
    id VARCHAR2(64) PRIMARY KEY,
    biz_type VARCHAR2(64),
    biz_id VARCHAR2(64),
    name VARCHAR2(128),
    tenant_id VARCHAR2(64),
    is_actived NUMBER(1) DEFAULT 1,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE ai_conversation_message (
    id VARCHAR2(64) PRIMARY KEY,
    conversation_id VARCHAR2(64) NOT NULL,
    question VARCHAR2(4000),
    prompt CLOB,
    answer CLOB,
    files CLOB,
    agents CLOB,
    at_items CLOB,
    extra_props CLOB,
    tenant_id VARCHAR2(64),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    CONSTRAINT chk_ai_conv_msg_files_json CHECK (files IS JSON),
    CONSTRAINT chk_ai_conv_msg_agents_json CHECK (agents IS JSON)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_datasource_db (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    datasource_id VARCHAR2(64) NOT NULL,
    name VARCHAR2(128) NOT NULL,
    type VARCHAR2(64),
    dbid VARCHAR2(64),
    description CLOB,
    status VARCHAR2(32) DEFAULT ''enabled'' NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_datasource_table (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    datasource_id VARCHAR2(64) NOT NULL,
    db_id VARCHAR2(64) NOT NULL,
    name VARCHAR2(128) NOT NULL,
    description CLOB,
    status VARCHAR2(32) DEFAULT ''enabled'' NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_datasource_field (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    datasource_id VARCHAR2(64) NOT NULL,
    db_id VARCHAR2(64) NOT NULL,
    table_id VARCHAR2(64) NOT NULL,
    name VARCHAR2(128) NOT NULL,
    type VARCHAR2(64),
    description CLOB,
    status VARCHAR2(32) DEFAULT ''enabled'' NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_app (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    menu_type VARCHAR2(32),
    title VARCHAR2(128) NOT NULL,
    icon VARCHAR2(128),
    page_id VARCHAR2(64),
    app_key VARCHAR2(64),
    redirect_url VARCHAR2(1024),
    parent_id VARCHAR2(64),
    ancestor_ids VARCHAR2(1024),
    sort NUMBER(10) DEFAULT 0,
    ancestor_sorts VARCHAR2(1024),
    level NUMBER(10),
    is_leaf NUMBER(1),
    has_watermark NUMBER(1) DEFAULT 0,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_dataset_folder (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    parent_id VARCHAR2(64),
    ancestor_ids VARCHAR2(1024),
    sort NUMBER(10),
    ancestor_sorts VARCHAR2(1024),
    level NUMBER(10),
    is_leaf NUMBER(1),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_dataset (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    folder_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    config CLOB,
    description VARCHAR2(1000),
    status VARCHAR2(32) DEFAULT ''enabled'' NOT NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
CREATE OR REPLACE TRIGGER trg_sys_operation_log_id
BEFORE INSERT ON sys_operation_log
FOR EACH ROW
BEGIN
  IF :NEW.id IS NULL THEN
    SELECT seq_sys_operation_log.NEXTVAL INTO :NEW.id FROM dual;
  END IF;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_permission (
    id VARCHAR2(64) PRIMARY KEY,
    parent_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    code VARCHAR2(64) NOT NULL,
    type VARCHAR2(32) NOT NULL,
    description VARCHAR2(1000),
    sort NUMBER(10) DEFAULT 0,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_role_permission (
    role_id VARCHAR2(64) NOT NULL,
    permission_id VARCHAR2(64) NOT NULL,
    PRIMARY KEY (role_id, permission_id)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_user_role (
    user_id VARCHAR2(64) NOT NULL,
    role_id VARCHAR2(64) NOT NULL,
    PRIMARY KEY (user_id, role_id)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_authorization (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    biz_type VARCHAR2(32) NOT NULL,
    biz_id VARCHAR2(64) NOT NULL,
    authorizer_scope VARCHAR2(32) NOT NULL,
    authorizer_id VARCHAR2(64),
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_share (
    id VARCHAR2(64) PRIMARY KEY,
    biz_type VARCHAR2(32) NOT NULL,
    biz_id VARCHAR2(64) NOT NULL,
    share_key VARCHAR2(64) NOT NULL,
    expire_type VARCHAR2(32) NOT NULL,
    expire_at TIMESTAMP(6) NULL,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_page_report (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    page_id VARCHAR2(64),
    title VARCHAR2(255),
    content CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_page_example (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    description VARCHAR2(1000),
    cover VARCHAR2(1024),
    item_json CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE bi_page_template (
    id VARCHAR2(64) PRIMARY KEY,
    tenant_id VARCHAR2(64),
    name VARCHAR2(128) NOT NULL,
    description VARCHAR2(1000),
    cover VARCHAR2(1024),
    schema_json CLOB,
    is_deleted NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP(6) NULL,
    created_by VARCHAR2(64),
    updated_at TIMESTAMP(6) NULL,
    updated_by VARCHAR2(64)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE sys_third_auth_user (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(64) NOT NULL,
    source VARCHAR2(32) NOT NULL,
    uuid VARCHAR2(128) NOT NULL,
    union_id VARCHAR2(128),
    username VARCHAR2(128),
    nickname VARCHAR2(128),
    avatar VARCHAR2(512),
    raw_data CLOB,
    create_time TIMESTAMP(6),
    update_time TIMESTAMP(6)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE SEQUENCE seq_sys_third_auth_user START WITH 1 INCREMENT BY 1';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
CREATE OR REPLACE TRIGGER trg_sys_third_auth_user_id
BEFORE INSERT ON sys_third_auth_user
FOR EACH ROW
BEGIN
  IF :NEW.id IS NULL THEN
    SELECT seq_sys_third_auth_user.NEXTVAL INTO :NEW.id FROM dual;
  END IF;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE INDEX idx_source_uuid ON sys_third_auth_user (source, uuid)';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE agentscope_sessions (
    session_id VARCHAR2(255) NOT NULL,
    state_key VARCHAR2(255) NOT NULL,
    item_index NUMBER(10) DEFAULT 0 NOT NULL,
    state_data CLOB NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, state_key, item_index)
  )';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/