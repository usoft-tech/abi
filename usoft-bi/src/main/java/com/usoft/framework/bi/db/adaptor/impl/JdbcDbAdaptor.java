package com.usoft.framework.bi.db.adaptor.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.usoft.framework.bi.api.DataSourceCreateRequest;
import com.usoft.framework.bi.api.db.schema.DatabaseSchema;
import com.usoft.framework.bi.service.DataSourceManager;
import com.usoft.framework.utils.SpringUtils;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcDbAdaptor extends BaseAdaptor {

    private final DataSource dataSource;

    public JdbcDbAdaptor(String id) {
        super(id);
        if (StringUtils.isBlank(id)) {
            this.dataSource = null;
        } else {
            this.dataSource = SpringUtils.getBean(DataSourceManager.class).getDataSource(id);
        }
    }

    @Override
    public boolean testConnection(DataSourceCreateRequest req) {
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName("bi-ds-" + id);
        ds.setDriverClassName(req.getDriverClassName());
        ds.setJdbcUrl(req.getUrl());
        ds.setUsername(req.getUsername());
        ds.setPassword(req.getPassword());
        try (Connection connection = ds.getConnection()) {
            return connection != null;
        } catch (Exception e) {
            log.error("测试数据库连接失败", e);
            return false;
        }
    }

    @Override
    public List<DatabaseSchema> getDatabaseSchemas() {
        if (dataSource == null) {
            return List.of();
        }
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData != null ? metaData.getDatabaseProductName() : null;
            String productVersion = metaData != null ? metaData.getDatabaseProductVersion() : null;

            String catalog = safeGetCatalog(connection);
            String schema = safeGetSchema(connection);

            Platform platform = createPlatform(dataSource);
            Database model = readModel(platform, "model", catalog, schema, new String[] { "TABLE", "VIEW" });

            if (model == null) {
                return List.of();
            }

            DatabaseSchema dbSchema = new DatabaseSchema();
            dbSchema.setName(firstNonBlank(catalog, schema, model.getName(), productName, "default"));
            dbSchema.setType(productName);
            dbSchema.setDescription(productVersion);
            dbSchema.setTables(convertTables(model.getTables()));

            return List.of(dbSchema);
        } catch (Exception e) {
            log.error("获取数据库模式失败", e);
            return List.of();
        }
    }

    private static String safeGetCatalog(Connection connection) {
        try {
            return connection.getCatalog();
        } catch (SQLException e) {
            return null;
        }
    }

    private static String safeGetSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (Throwable e) {
            return null;
        }
    }

    private static Platform createPlatform(DataSource dataSource) {
        try {
            return PlatformFactory.createNewPlatformInstance(dataSource);
        } catch (Throwable ignore) {
            try {
                Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
                platform.setDataSource(dataSource);
                return platform;
            } catch (Throwable e) {
                throw new IllegalStateException("创建 DdlUtils Platform 失败", e);
            }
        }
    }

    private static Database readModel(
            Platform platform,
            String modelName,
            String catalog,
            String schema,
            String[] tableTypes) {
        Objects.requireNonNull(platform, "platform");
        try {
            return platform.readModelFromDatabase(modelName, catalog, schema, tableTypes);
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static DatabaseSchema.Table[] convertTables(Table[] tables) {
        if (tables == null || tables.length == 0) {
            return new DatabaseSchema.Table[0];
        }
        List<DatabaseSchema.Table> result = new ArrayList<>(tables.length);
        for (Table t : tables) {
            if (t == null) {
                continue;
            }
            DatabaseSchema.Table table = new DatabaseSchema.Table();
            table.setName(t.getName());
            table.setDescription(t.getDescription());
            table.setColumns(convertColumns(t.getColumns()));
            result.add(table);
        }
        return result.toArray(DatabaseSchema.Table[]::new);
    }

    private static DatabaseSchema.Column[] convertColumns(Column[] columns) {
        if (columns == null || columns.length == 0) {
            return new DatabaseSchema.Column[0];
        }
        List<DatabaseSchema.Column> result = new ArrayList<>(columns.length);
        for (Column c : columns) {
            if (c == null) {
                continue;
            }
            DatabaseSchema.Column col = new DatabaseSchema.Column();
            col.setName(c.getName());
            col.setType(c.getType());
            col.setDescription(c.getDescription());
            result.add(col);
        }
        return result.toArray(DatabaseSchema.Column[]::new);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
