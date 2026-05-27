package com.usoft.framework.bi.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.usoft.framework.bi.entity.DataSourceEntity;
import com.usoft.framework.bi.mapper.DataSourceMapper;
import com.usoft.framework.core.tenant.TenantContext;
import com.zaxxer.hikari.HikariDataSource;

@Service
public class DataSourceManager {
    private final DataSourceMapper dataSourceMapper;

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    public DataSourceManager(DataSourceMapper dataSourceMapper) {
        this.dataSourceMapper = dataSourceMapper;
    }

    public DataSource getDataSource(String id) {
        HikariDataSource ds = dataSources.get(id);
        if (ds != null) {
            return ds;
        }
        String tenantId = TenantContext.getTenantId();
        DataSourceEntity e = dataSourceMapper.findByIdInTenant(id, tenantId);
        if (e == null) {
            return null;
        }
        String driver = e.getDriverClassName();
        if (driver == null || driver.isBlank()) {
            driver = resolveDriver(e.getType());
        }
        ds = new HikariDataSource();
        ds.setPoolName("bi-ds-" + id);
        ds.setDriverClassName(driver);
        ds.setJdbcUrl(e.getUrl());
        ds.setUsername(e.getUsername());
        ds.setPassword(e.getPassword());    
        dataSources.put(id, ds);
        return ds;
    }

    public void evict(String id) {
        HikariDataSource remove = dataSources.remove(id);
        if (remove != null) {
            remove.close();
        }
    }

    private String resolveDriver(String type) {
        if (type == null) {
            return null;
        }
        String t = type.toLowerCase();
        if (t.equals("mysql"))
            return "com.mysql.cj.jdbc.Driver";
        if (t.equals("pgsql") || t.equals("postgresql"))
            return "org.postgresql.Driver";
        if (t.equals("sqlserver"))
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        if (t.equals("oracle"))
            return "oracle.jdbc.OracleDriver";
        if (t.equals("h2"))
            return "org.h2.Driver";
        return null;
    }
}
