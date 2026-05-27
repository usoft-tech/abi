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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import io.agentscope.core.session.Session;

/**
 * Factory for creating Session instances based on the DataSource type.
 */
public class JdbcSessionFactory {

    /**
     * Create a Session instance based on the DataSource type.
     *
     * @param dataSource DataSource for database connections
     * @return A Session instance (MysqlSession or PostgreSession)
     * @throws RuntimeException if the database type is not supported
     */
    public static Session createSession(DataSource dataSource) {
        return createSession(dataSource, null);
    }

    /**
     * Create a Session instance based on the DataSource type with a custom table name.
     *
     * @param dataSource DataSource for database connections
     * @param tableName Custom table name
     * @return A Session instance (MysqlSession or PostgreSession)
     * @throws RuntimeException if the database type is not supported
     */
    public static Session createSession(DataSource dataSource, String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();

            if (databaseProductName.contains("mysql")) {
                return new MysqlSession(dataSource, tableName);
            } else if (databaseProductName.contains("postgresql")) {
                return new PostgreSession(dataSource, tableName);
            } else {
                throw new UnsupportedOperationException("Unsupported database type: " + databaseProductName);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to detect database type from DataSource", e);
        }
    }
}
