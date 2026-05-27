package com.usoft.framework.bi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.DatasourceFieldQueryRequest;
import com.usoft.framework.bi.api.DataSourceCreateRequest;
import com.usoft.framework.bi.api.DataSourceQueryRequest;
import com.usoft.framework.bi.api.DataSourceResponse;
import com.usoft.framework.bi.api.DataSourceUpdateRequest;
import com.usoft.framework.bi.api.db.schema.DatabaseSchema;
import com.usoft.framework.bi.db.adaptor.DbAdaptorFactory;
import com.usoft.framework.bi.entity.DatasourceDbEntity;
import com.usoft.framework.bi.entity.DatasourceFieldEntity;
import com.usoft.framework.bi.entity.DatasourceTableEntity;
import com.usoft.framework.bi.entity.DataSourceEntity;
import com.usoft.framework.bi.mapper.DatasourceDbMapper;
import com.usoft.framework.bi.mapper.DatasourceFieldMapper;
import com.usoft.framework.bi.mapper.DatasourceTableMapper;
import com.usoft.framework.bi.mapper.DataSourceMapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataSourceService {
    private final DataSourceMapper dataSourceMapper;
    private final DataSourceManager dataSourceManager;
    private final DatasourceDbMapper datasourceDbMapper;
    private final DatasourceTableMapper datasourceTableMapper;
    private final DatasourceFieldMapper datasourceFieldMapper;
    private final TenantService tenantService;

    public DataSourceResponse create(DataSourceCreateRequest req) {
        DataSourceEntity e = new DataSourceEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        dataSourceMapper.insert(e);
        refreshDatasourceSchemas(e.getId(), e.getType());
        DataSourceResponse r = new DataSourceResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    public DataSourceResponse update(String id, DataSourceUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        DataSourceEntity e = dataSourceMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        String password = e.getPassword();
        BeanMapper.mapper(req, e);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            e.setPassword(req.getPassword());
        } else {
            e.setPassword(password);
        }
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        dataSourceMapper.update(e);
        dataSourceManager.evict(id);
        refreshDatasourceSchemas(e.getId(), e.getType());
        DataSourceResponse r = new DataSourceResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        DataSourceEntity e = dataSourceMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        boolean ok = dataSourceMapper.update(e) > 0;
        if (ok) {
            dataSourceManager.evict(id);
        }
        return ok;
    }

    public DataSourceResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        DataSourceEntity e = dataSourceMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        DataSourceResponse r = new DataSourceResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    public PageResponse<DataSourceResponse> list(DataSourceQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getType() != null && !req.getType().isBlank()) {
            qw.and("type = ?", req.getType());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ? OR type LIKE ? OR url LIKE ?)", kw, kw, kw);
        }
        return PageHelper.apply(dataSourceMapper, qw, req, e -> {
            DataSourceResponse r = new DataSourceResponse();
            BeanMapper.mapper(e, r);
            return r;
        });
    }

    public void syncSchema(String id) {
        DataSourceEntity e = dataSourceMapper.selectOneById(id);
        if (e != null) {
            tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
            refreshDatasourceSchemas(e.getId(), e.getType());
        }
    }

    public List<DatasourceDbEntity> listDbs(String datasourceId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("datasource_id = ?", datasourceId)
                .and("is_deleted = 0");
        return datasourceDbMapper.selectListByQuery(qw);
    }

    
    public List<DatasourceDbEntity> activeListDbs() {
        QueryWrapper qw = QueryWrapper.create()
                .and("status = ?", EnableStatus.ENABLE)
                .and("is_deleted = 0");
        return datasourceDbMapper.selectListByQuery(qw);
    }
    
    public List<DatasourceDbEntity> activeListDbs(List<String> dbIds) {
        if (CollectionUtils.isEmpty(dbIds)) {
            return Collections.emptyList();
        }
        QueryWrapper qw = QueryWrapper.create()
                .in("id", dbIds)
                .and("status = ?", EnableStatus.ENABLE)
                .and("is_deleted = 0");
        return datasourceDbMapper.selectListByQuery(qw);
    }

    public List<DatasourceTableEntity> listTables(String dbId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("db_id = ?", dbId)
                .and("is_deleted = 0");
        return datasourceTableMapper.selectListByQuery(qw);
    }

    public List<DatasourceTableEntity> activeListTables(List<String> dbIds) {
        QueryWrapper qw = QueryWrapper.create()
                .in("db_id", dbIds)
                .and("status = ?", EnableStatus.ENABLE)
                .and("is_deleted = 0");
        return datasourceTableMapper.selectListByQuery(qw);
    }

    public List<DatasourceFieldEntity> activeListFields(List<String> dbIds) {
        QueryWrapper qw = QueryWrapper.create()
                .in("db_id", dbIds)
                .and("status = ?", EnableStatus.ENABLE)
                .and("is_deleted = 0");
        return datasourceFieldMapper.selectListByQuery(qw);
    }

    public PageResponse<DatasourceFieldEntity> pageFields(DatasourceFieldQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("is_deleted = 0");
        if (req.getDatasourceId() != null && !req.getDatasourceId().isBlank()) {
            qw.and("datasource_id = ?", req.getDatasourceId());
        }
        if (req.getDbId() != null && !req.getDbId().isBlank()) {
            qw.and("db_id = ?", req.getDbId());
        }
        if (req.getTableId() != null && !req.getTableId().isBlank()) {
            qw.and("table_id = ?", req.getTableId());
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        return PageHelper.apply(datasourceFieldMapper, qw, req, e -> e);
    }

    public boolean updateDbStatus(String id, EnableStatus status) {
        DatasourceDbEntity e = datasourceDbMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceDbMapper.update(e) > 0;
    }

    public boolean updateTableStatus(String id, EnableStatus status) {
        DatasourceTableEntity e = datasourceTableMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceTableMapper.update(e) > 0;
    }

    public boolean updateFieldStatus(String id, EnableStatus status) {
        DatasourceFieldEntity e = datasourceFieldMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceFieldMapper.update(e) > 0;
    }

    public boolean updateDbDescription(String id, String description) {
        DatasourceDbEntity e = datasourceDbMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setDescription(description);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceDbMapper.update(e) > 0;
    }

    public boolean updateTableDescription(String id, String description) {
        DatasourceTableEntity e = datasourceTableMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setDescription(description);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceTableMapper.update(e) > 0;
    }

    public boolean updateFieldDescription(String id, String description) {
        DatasourceFieldEntity e = datasourceFieldMapper.selectOneById(id);
        if (e == null)
            return false;
        e.setDescription(description);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasourceFieldMapper.update(e) > 0;
    }

    private void refreshDatasourceSchemas(String datasourceId, String datasourceType) {
        String tenantId = TenantContext.getTenantId();

        // 1. 查出旧数据待用，保留描述信息
        List<DatasourceDbEntity> oldDbs = datasourceDbMapper.selectListByQuery(
                QueryWrapper.create().where("datasource_id = ?", datasourceId).and("is_deleted = 0"));
        List<DatasourceTableEntity> oldTables = datasourceTableMapper.selectListByQuery(
                QueryWrapper.create().where("datasource_id = ?", datasourceId).and("is_deleted = 0"));
        List<DatasourceFieldEntity> oldFields = datasourceFieldMapper.selectListByQuery(
                QueryWrapper.create().where("datasource_id = ?", datasourceId).and("is_deleted = 0"));

        Map<String, String> oldDbDescMap = new HashMap<>();
        Map<String, String> dbIdToName = new HashMap<>();
        for (DatasourceDbEntity db : oldDbs) {
            oldDbDescMap.put(db.getName(), db.getDescription());
            dbIdToName.put(db.getId(), db.getName());
        }

        Map<String, Map<String, String>> oldTableDescMap = new HashMap<>();
        Map<String, String> tableIdToDbName = new HashMap<>();
        Map<String, String> tableIdToTableName = new HashMap<>();
        for (DatasourceTableEntity t : oldTables) {
            String dbName = dbIdToName.get(t.getDbId());
            if (dbName != null) {
                oldTableDescMap.computeIfAbsent(dbName, k -> new HashMap<>()).put(t.getName(), t.getDescription());
                tableIdToDbName.put(t.getId(), dbName);
                tableIdToTableName.put(t.getId(), t.getName());
            }
        }

        Map<String, Map<String, Map<String, String>>> oldFieldDescMap = new HashMap<>();
        for (DatasourceFieldEntity f : oldFields) {
            String dbName = tableIdToDbName.get(f.getTableId());
            String tableName = tableIdToTableName.get(f.getTableId());
            if (dbName != null && tableName != null) {
                oldFieldDescMap.computeIfAbsent(dbName, k -> new HashMap<>())
                        .computeIfAbsent(tableName, k -> new HashMap<>())
                        .put(f.getName(), f.getDescription());
            }
        }

        // 2. 删除旧数据
        datasourceFieldMapper.deleteByQuery(QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("datasource_id = ?", datasourceId));
        datasourceTableMapper.deleteByQuery(QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("datasource_id = ?", datasourceId));
        datasourceDbMapper.deleteByQuery(QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("datasource_id = ?", datasourceId));

        List<DatabaseSchema> schemas = DbAdaptorFactory.getDbAdaptor(datasourceType, datasourceId).getDatabaseSchemas();
        if (schemas == null || schemas.isEmpty()) {
            return;
        }

        String username = UserHolder.username();
        Instant now = Instant.now();
        for (DatabaseSchema s : schemas) {
            String dbId = UUID.randomUUID().toString();
            DatasourceDbEntity db = new DatasourceDbEntity();
            db.setId(dbId);
            db.setTenantId(tenantId);
            db.setDatasourceId(datasourceId);
            db.setName(s.getName());
            db.setType(s.getType());
            db.setDbid(s.getDbid());

            // 尝试使用旧描述
            String desc = s.getDescription();
            if ((desc == null || desc.isBlank()) && oldDbDescMap.containsKey(s.getName())) {
                desc = oldDbDescMap.get(s.getName());
            }
            db.setDescription(desc);

            db.setStatus(EnableStatus.DISABLE);
            db.setIsDeleted(false);
            db.setCreatedAt(now);
            db.setCreatedBy(username);
            db.setUpdatedAt(now);
            db.setUpdatedBy(username);
            datasourceDbMapper.insert(db);

            DatabaseSchema.Table[] tables = s.getTables();
            if (tables == null || tables.length == 0) {
                continue;
            }
            for (DatabaseSchema.Table t : tables) {
                if (t == null) {
                    continue;
                }
                String tableId = UUID.randomUUID().toString();
                DatasourceTableEntity table = new DatasourceTableEntity();
                table.setId(tableId);
                table.setTenantId(tenantId);
                table.setDatasourceId(datasourceId);
                table.setDbId(dbId);
                table.setName(t.getName());

                // 尝试使用旧描述
                String tDesc = t.getDescription();
                if ((tDesc == null || tDesc.isBlank())) {
                    Map<String, String> tablesMap = oldTableDescMap.get(s.getName());
                    if (tablesMap != null && tablesMap.containsKey(t.getName())) {
                        tDesc = tablesMap.get(t.getName());
                    }
                }
                table.setDescription(tDesc);

                table.setStatus(EnableStatus.ENABLE);
                table.setIsDeleted(false);
                table.setCreatedAt(now);
                table.setCreatedBy(username);
                table.setUpdatedAt(now);
                table.setUpdatedBy(username);
                datasourceTableMapper.insert(table);

                DatabaseSchema.Column[] columns = t.getColumns();
                if (columns == null || columns.length == 0) {
                    continue;
                }
                List<DatasourceFieldEntity> fields = new ArrayList<>(columns.length);
                for (DatabaseSchema.Column c : columns) {
                    if (c == null) {
                        continue;
                    }
                    DatasourceFieldEntity field = new DatasourceFieldEntity();
                    field.setId(UUID.randomUUID().toString());
                    field.setTenantId(tenantId);
                    field.setDatasourceId(datasourceId);
                    field.setDbId(dbId);
                    field.setTableId(tableId);
                    field.setName(c.getName());
                    field.setType(c.getType());

                    // 尝试使用旧描述
                    String cDesc = c.getDescription();
                    if ((cDesc == null || cDesc.isBlank())) {
                        Map<String, Map<String, String>> tablesMap = oldFieldDescMap.get(s.getName());
                        if (tablesMap != null) {
                            Map<String, String> fieldsMap = tablesMap.get(t.getName());
                            if (fieldsMap != null && fieldsMap.containsKey(c.getName())) {
                                cDesc = fieldsMap.get(c.getName());
                            }
                        }
                    }
                    field.setDescription(cDesc);

                    field.setStatus(EnableStatus.ENABLE);
                    field.setIsDeleted(false);
                    field.setCreatedAt(now);
                    field.setCreatedBy(username);
                    field.setUpdatedAt(now);
                    field.setUpdatedBy(username);
                    fields.add(field);
                }
                for (DatasourceFieldEntity f : fields) {
                    datasourceFieldMapper.insert(f);
                }
            }
        }
    }

    public DatasourceDbEntity getDb(String dbId) {
        return datasourceDbMapper.selectOneById(dbId);
    }
}
