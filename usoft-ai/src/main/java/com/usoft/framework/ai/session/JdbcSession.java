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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import io.agentscope.core.session.ListHashUtil;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import lombok.Getter;

/**
 * Base class for JDBC-based session implementations.
 */
public abstract class JdbcSession implements Session {

    protected static final String DEFAULT_TABLE_NAME = "agentscope_sessions";

    /** Suffix for hash storage keys. */
    protected static final String HASH_KEY_SUFFIX = ":_hash";

    /** item_index value for single state values. */
    protected static final int SINGLE_STATE_INDEX = 0;

    /**
     * Pattern for validating database and table names. Only allows alphanumeric characters and
     * underscores, must start with letter or underscore. This prevents SQL injection attacks
     * through malicious database/table names.
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private static final int MAX_IDENTIFIER_LENGTH = 64; // Database identifier length limit

    @Getter
    protected final DataSource dataSource;
    @Getter
    protected final String tableName;

    /**
     * Create a JdbcSession with default settings.
     *
     * @param dataSource DataSource for database connections
     */
    protected JdbcSession(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }

    /**
     * Create a JdbcSession with custom table name.
     *
     * @param dataSource DataSource for database connections
     * @param tableName Custom table name
     */
    protected JdbcSession(DataSource dataSource, String tableName) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }

        this.dataSource = dataSource;
        this.tableName = (tableName == null || tableName.trim().isEmpty())
                ? DEFAULT_TABLE_NAME
                : tableName.trim();

        // Validate table names to prevent SQL injection
        validateIdentifier(this.tableName, "Table name");
    }

    /**
     * Get the UPSERT SQL statement for this database.
     *
     * @return UPSERT SQL
     */
    protected abstract String getUpsertSql();

    @Override
    public void save(SessionKey sessionKey, String key, State value) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        String upsertSql = getUpsertSql();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertSql)) {

            String json = JsonUtils.getJsonCodec().toJson(value);

            stmt.setString(1, sessionId);
            stmt.setString(2, key);
            stmt.setInt(3, SINGLE_STATE_INDEX);
            stmt.setString(4, json);

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save state: " + key, e);
        }
    }

    @Override
    public void save(SessionKey sessionKey, String key, List<? extends State> values) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        if (values.isEmpty()) {
            return;
        }

        String hashKey = key + HASH_KEY_SUFFIX;

        try (Connection conn = dataSource.getConnection()) {
            // Compute current hash
            String currentHash = ListHashUtil.computeHash(values);

            // Get stored hash
            String storedHash = getStoredHash(conn, sessionId, hashKey);

            // Get existing count
            int existingCount = getListCount(conn, sessionId, key);

            // Determine if full rewrite is needed
            boolean needsFullRewrite = ListHashUtil.needsFullRewrite(
                    currentHash, storedHash, values.size(), existingCount);

            if (needsFullRewrite) {
                // Transaction: delete all + insert all
                conn.setAutoCommit(false);
                try {
                    deleteListItems(conn, sessionId, key);
                    insertAllItems(conn, sessionId, key, values);
                    saveHash(conn, sessionId, hashKey, currentHash);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } else if (values.size() > existingCount) {
                // Incremental append
                List<? extends State> newItems = values.subList(existingCount, values.size());
                insertItems(conn, sessionId, key, newItems, existingCount);
                saveHash(conn, sessionId, hashKey, currentHash);
            }
            // else: no change, skip

        } catch (Exception e) {
            throw new RuntimeException("Failed to save list: " + key, e);
        }
    }

    protected String getStoredHash(Connection conn, String sessionId, String hashKey)
            throws SQLException {
        String selectSql = "SELECT state_data FROM " + getTableName()
                + " WHERE session_id = ? AND state_key = ? AND item_index = ?";

        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, hashKey);
            stmt.setInt(3, SINGLE_STATE_INDEX);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("state_data");
                }
                return null;
            }
        }
    }

    protected void saveHash(Connection conn, String sessionId, String hashKey, String hash)
            throws SQLException {
        String upsertSql = getUpsertSql();

        try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, hashKey);
            stmt.setInt(3, SINGLE_STATE_INDEX);
            stmt.setString(4, hash);
            stmt.executeUpdate();
        }
    }

    protected void deleteListItems(Connection conn, String sessionId, String key)
            throws SQLException {
        String deleteSql = "DELETE FROM " + getTableName() + " WHERE session_id = ? AND state_key = ?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, key);
            stmt.executeUpdate();
        }
    }

    protected void insertAllItems(Connection conn, String sessionId, String key, List<? extends State> values)
            throws Exception {
        insertItems(conn, sessionId, key, values, 0);
    }

    protected void insertItems(Connection conn, String sessionId, String key, List<? extends State> items, int startIndex)
            throws Exception {
        String upsertSql = getUpsertSql();

        try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
            int index = startIndex;
            for (State item : items) {
                String json = JsonUtils.getJsonCodec().toJson(item);
                stmt.setString(1, sessionId);
                stmt.setString(2, key);
                stmt.setInt(3, index);
                stmt.setString(4, json);
                stmt.addBatch();
                index++;
            }
            stmt.executeBatch();
        }
    }

    protected int getListCount(Connection conn, String sessionId, String key) throws SQLException {
        String selectSql = "SELECT MAX(item_index) as max_index FROM " + getTableName()
                + " WHERE session_id = ? AND state_key = ?";

        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int maxIndex = rs.getInt("max_index");
                    if (rs.wasNull()) {
                        return 0;
                    }
                    return maxIndex + 1;
                }
                return 0;
            }
        }
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        String selectSql = "SELECT state_data FROM " + getTableName()
                + " WHERE session_id = ? AND state_key = ? AND item_index = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setString(1, sessionId);
            stmt.setString(2, key);
            stmt.setInt(3, SINGLE_STATE_INDEX);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("state_data");
                    return Optional.of(JsonUtils.getJsonCodec().fromJson(json, type));
                }
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get state: " + key, e);
        }
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> itemType) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        String selectSql = "SELECT state_data FROM " + getTableName()
                + " WHERE session_id = ? AND state_key = ?"
                + " ORDER BY item_index";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {

            stmt.setString(1, sessionId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    String json = rs.getString("state_data");
                    result.add(JsonUtils.getJsonCodec().fromJson(json, itemType));
                }
                return result;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get list: " + key, e);
        }
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

        String existsSql = "SELECT 1 FROM " + getTableName() + " WHERE session_id = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(existsSql)) {

            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check session existence: " + sessionId, e);
        }
    }

    @Override
    public void delete(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

        String deleteSql = "DELETE FROM " + getTableName() + " WHERE session_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

            stmt.setString(1, sessionId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete session: " + sessionId, e);
        }
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
        String listSql = "SELECT DISTINCT session_id FROM " + getTableName() + " ORDER BY session_id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(listSql);
             ResultSet rs = stmt.executeQuery()) {

            Set<SessionKey> sessionKeys = new HashSet<>();
            while (rs.next()) {
                sessionKeys.add(SimpleSessionKey.of(rs.getString("session_id")));
            }
            return sessionKeys;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to list sessions", e);
        }
    }

    @Override
    public void close() {
        // DataSource is managed externally
    }

    public int clearAllSessions() {
        String clearSql = "DELETE FROM " + getTableName();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(clearSql)) {

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear sessions", e);
        }
    }

    protected void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (sessionId.contains("/") || sessionId.contains("\\")) {
            throw new IllegalArgumentException("Session ID cannot contain path separators");
        }
        if (sessionId.length() > 255) {
            throw new IllegalArgumentException("Session ID cannot exceed 255 characters");
        }
    }

    protected void validateStateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("State key cannot be null or empty");
        }
        if (key.length() > 255) {
            throw new IllegalArgumentException("State key cannot exceed 255 characters");
        }
    }

    protected void validateIdentifier(String identifier, String identifierType) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException(identifierType + " cannot be null or empty");
        }
        if (identifier.length() > MAX_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException(identifierType + " cannot exceed " + MAX_IDENTIFIER_LENGTH + " characters");
        }
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException(identifierType + " contains invalid characters. Only alphanumeric characters and underscores are allowed, and it must start with a letter or underscore. Invalid value: " + identifier);
        }
    }
}
