/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.usoft.framework.ai.session;

import javax.sql.DataSource;

/**
 * database-based session implementation for MySQL.
 */
public class MysqlSession extends JdbcSession {

    /**
     * Create a MysqlSession with default settings.
     *
     * @param dataSource DataSource for database connections
     */
    public MysqlSession(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Create a MysqlSession with custom table name.
     *
     * @param dataSource DataSource for database connections
     * @param tableName Custom table name
     */
    public MysqlSession(DataSource dataSource, String tableName) {
        super(dataSource, tableName);
    }

    @Override
    protected String getUpsertSql() {
        return "INSERT INTO " + getTableName()
                + " (session_id, state_key, item_index, state_data) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE state_data = VALUES(state_data)";
    }
}
