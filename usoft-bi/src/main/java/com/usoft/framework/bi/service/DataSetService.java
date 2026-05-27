package com.usoft.framework.bi.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.ai.AgentScopeProxy;
import com.usoft.framework.bi.api.DataSetAssistantChatRequest;
import com.usoft.framework.bi.api.DataSetExeResponse;
import com.usoft.framework.bi.api.DataSourceResponse;
import com.usoft.framework.bi.api.DatasetCreateRequest;
import com.usoft.framework.bi.api.DatasetFolderCreateRequest;
import com.usoft.framework.bi.api.DatasetFolderUpdateRequest;
import com.usoft.framework.bi.api.DatasetQueryRequest;
import com.usoft.framework.bi.api.DatasetResponse;
import com.usoft.framework.bi.api.DatasetUpdateRequest;
import com.usoft.framework.bi.api.GenerateSqlRequest;
import com.usoft.framework.bi.api.GenerateSqlResponse;
import com.usoft.framework.bi.api.TestSqlRequest;
import com.usoft.framework.bi.api.dataset.schema.DataSetConfig;
import com.usoft.framework.bi.api.dataset.schema.DataSetConfig.Input;
import com.usoft.framework.bi.api.dataset.schema.DataSetConfig.Sql;
import com.usoft.framework.bi.chatbi.ChatBiClient;
import com.usoft.framework.bi.config.PromptProperties;
import com.usoft.framework.bi.entity.DatasetEntity;
import com.usoft.framework.bi.entity.DatasetFolderEntity;
import com.usoft.framework.bi.entity.DatasourceDbEntity;
import com.usoft.framework.bi.entity.DatasourceFieldEntity;
import com.usoft.framework.bi.entity.DatasourceTableEntity;
import com.usoft.framework.bi.mapper.DatasetFolderMapper;
import com.usoft.framework.bi.mapper.DatasetMapper;
import com.usoft.framework.bi.service.dataset.DatasetTargetSchemaGeneratorProxy;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.enums.EnableStatus;
import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.utils.ExcelUtils.CachedExcelDatabase;
import com.usoft.framework.utils.ExcelUtils.ExcelSchema;
import com.usoft.framework.utils.MybatisUtils.BoundingSql;
import com.usoft.framework.utils.GraalJsUtil;
import com.usoft.framework.utils.MybatisUtils;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.utils.SpringElUtil;
import com.zaxxer.hikari.HikariDataSource;

import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Select;

@Service
@RequiredArgsConstructor
public class DataSetService {

    private final DataSourceManager dataSourceManager;

    private final DataSourceService dataSourceService;

    private final PageService pageService;

    private final TenantService tenantService;

    private final DatasetFolderMapper folderMapper;
    private final DatasetMapper datasetMapper;

    private final PromptProperties promptProperties;

    private final AgentScopeProxy agentScopeProxy;

    // --- Folder Operations ---

    @Transactional
    public DatasetFolderEntity createFolder(DatasetFolderCreateRequest req) {
        DatasetFolderEntity e = new DatasetFolderEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        e.setIsLeaf(true); // Newly created folder is a leaf initially

        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            DatasetFolderEntity parent = folderMapper.selectOneById(req.getParentId());
            if (parent != null) {
                e.setParentId(parent.getId());
                e.setLevel((parent.getLevel() == null ? 0 : parent.getLevel()) + 1);
                String parentAncestors = parent.getAncestorIds() == null ? "" : parent.getAncestorIds();
                e.setAncestorIds(parentAncestors + parent.getId() + ",");

                String parentSorts = parent.getAncestorSorts() == null ? "" : parent.getAncestorSorts();
                // Assuming sort is used for ordering, ancestorSorts might be useful for full
                // path sorting
                // For now, just appending parent's sort or keeping it simple
                e.setAncestorSorts(parentSorts + (parent.getSort() == null ? 0 : parent.getSort()) + ",");

                // Update parent to not be leaf
                if (Boolean.TRUE.equals(parent.getIsLeaf())) {
                    parent.setIsLeaf(false);
                    folderMapper.update(parent);
                }
            } else {
                // Treat as root if parent not found
                e.setParentId(null);
                e.setLevel(0);
                e.setAncestorIds("");
                e.setAncestorSorts("");
            }
        } else {
            e.setLevel(0);
            e.setAncestorIds("");
            e.setAncestorSorts("");
        }

        folderMapper.insert(e);
        return e;
    }

    @Transactional
    public DatasetFolderEntity updateFolder(String id, DatasetFolderUpdateRequest req) {
        DatasetFolderEntity e = folderMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        String oldParentId = e.getParentId();
        Integer oldLevel = e.getLevel();
        String oldAncestorIds = e.getAncestorIds();

        // Basic fields
        if (req.getName() != null) {
            e.setName(req.getName());
        }
        if (req.getSort() != null) {
            e.setSort(req.getSort());
        }

        // Handle move
        if (req.getParentId() != null) {
            String newParentId = req.getParentId().isEmpty() ? null : req.getParentId();

            if (!java.util.Objects.equals(oldParentId, newParentId)) {
                // Validation
                if (id.equals(newParentId)) {
                    throw new RuntimeException("不能移动到自身");
                }

                String newAncestorIds = "";
                String newAncestorSorts = "";
                Integer newLevel = 0;

                if (newParentId != null) {
                    DatasetFolderEntity newParent = folderMapper.selectOneById(newParentId);
                    if (newParent == null) {
                        throw new RuntimeException("目标文件夹不存在");
                    }
                    // Check circular
                    if (newParent.getAncestorIds() != null && newParent.getAncestorIds().contains(id + ",")) {
                        throw new RuntimeException("不能移动到子文件夹下");
                    }

                    newAncestorIds = (newParent.getAncestorIds() == null ? "" : newParent.getAncestorIds())
                            + newParent.getId() + ",";
                    newAncestorSorts = (newParent.getAncestorSorts() == null ? "" : newParent.getAncestorSorts())
                            + (newParent.getSort() == null ? 0 : newParent.getSort()) + ",";
                    newLevel = (newParent.getLevel() == null ? 0 : newParent.getLevel()) + 1;

                    // Update new parent leaf status
                    if (Boolean.TRUE.equals(newParent.getIsLeaf())) {
                        newParent.setIsLeaf(false);
                        folderMapper.update(newParent);
                    }
                }

                // Update current node
                e.setParentId(newParentId);
                e.setAncestorIds(newAncestorIds);
                e.setAncestorSorts(newAncestorSorts);
                e.setLevel(newLevel);

                // Update descendants
                QueryWrapper descendantsQw = QueryWrapper.create()
                        .where("ancestor_ids LIKE ?", "%" + id + ",%");
                List<DatasetFolderEntity> descendants = folderMapper.selectListByQuery(descendantsQw);

                String oldPrefix = (oldAncestorIds == null ? "" : oldAncestorIds) + id + ",";
                String newPrefix = newAncestorIds + id + ",";
                int levelDelta = newLevel - (oldLevel == null ? 0 : oldLevel);

                for (DatasetFolderEntity desc : descendants) {
                    if (desc.getAncestorIds() != null) {
                        desc.setAncestorIds(desc.getAncestorIds().replace(oldPrefix, newPrefix));
                    }
                    if (desc.getLevel() != null) {
                        desc.setLevel(desc.getLevel() + levelDelta);
                    }
                    folderMapper.update(desc);
                }

                // Check old parent leaf status
                if (oldParentId != null) {
                    long siblingsCount = folderMapper.selectCountByQuery(QueryWrapper.create()
                            .where("parent_id = ?", oldParentId)
                            .and("is_deleted = 0")
                            .and("id != ?", id));

                    if (siblingsCount == 0) {
                        DatasetFolderEntity oldParent = new DatasetFolderEntity();
                        oldParent.setId(oldParentId);
                        oldParent.setIsLeaf(true);
                        folderMapper.update(oldParent);
                    }
                }
            }
        }

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        folderMapper.update(e);
        return e;
    }

    @Transactional
    public boolean deleteFolder(String id) {
        DatasetFolderEntity e = folderMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        // Find all folders to delete (self + descendants)
        QueryWrapper idsQw = QueryWrapper.create()
                .select("id")
                .where("id = ?", id)
                .or("ancestor_ids LIKE ?", "%" + id + ",%");
        List<String> folderIdsToDelete = folderMapper.selectObjectListByQueryAs(idsQw, String.class);

        if (folderIdsToDelete == null || folderIdsToDelete.isEmpty()) {
            return false;
        }

        String username = UserHolder.username();
        Instant now = Instant.now();

        // 1. Soft delete folders
        DatasetFolderEntity folderUpdate = new DatasetFolderEntity();
        folderUpdate.setIsDeleted(true);
        folderUpdate.setUpdatedAt(now);
        folderUpdate.setUpdatedBy(username);

        QueryWrapper folderUpdateQw = QueryWrapper.create()
                .where("id IN (?)", folderIdsToDelete);
        folderMapper.updateByQuery(folderUpdate, folderUpdateQw);

        // 2. Soft delete datasets in these folders
        DatasetEntity datasetUpdate = new DatasetEntity();
        datasetUpdate.setIsDeleted(true);
        datasetUpdate.setUpdatedAt(now);
        datasetUpdate.setUpdatedBy(username);

        QueryWrapper datasetUpdateQw = QueryWrapper.create()
                .where("folder_id IN (?)", folderIdsToDelete);
        datasetMapper.updateByQuery(datasetUpdate, datasetUpdateQw);

        // 3. Update parent's isLeaf status if needed
        if (e.getParentId() != null) {
            long siblingsCount = folderMapper.selectCountByQuery(QueryWrapper.create()
                    .where("parent_id = ?", e.getParentId())
                    .and("is_deleted = 0"));

            if (siblingsCount == 0) {
                DatasetFolderEntity parentUpdate = new DatasetFolderEntity();
                parentUpdate.setId(e.getParentId());
                parentUpdate.setIsLeaf(true);
                parentUpdate.setUpdatedAt(now);
                parentUpdate.setUpdatedBy(username);
                folderMapper.update(parentUpdate);
            }
        }

        return true;
    }

    public List<DatasetFolderEntity> listFolders() {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", TenantContext.getTenantId())
                .and("is_deleted = 0")
                .orderBy("sort", true);
        return folderMapper.selectListByQuery(qw);
    }

    public DatasetFolderEntity getFolder(String id) {
        return folderMapper.selectOneById(id);
    }

    // --- Dataset Operations ---

    public DatasetResponse createDataset(DatasetCreateRequest req) {
        DatasetEntity e = new DatasetEntity();
        BeanMapper.mapper(req, e);
        e.setConfig(ObjectMapperUtils.toJson(req.getConfig()));
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setStatus(EnableStatus.ENABLE);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        datasetMapper.insert(e);

        DatasetResponse r = new DatasetResponse();
        BeanMapper.mapper(e, r);
        r.setConfig(ObjectMapperUtils.fromJson(e.getConfig(), DataSetConfig.class));
        return r;
    }

    public DatasetResponse updateDataset(String id, DatasetUpdateRequest req) {
        DatasetEntity e = datasetMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        BeanMapper.mapper(req, e);
        e.setConfig(ObjectMapperUtils.toJson(req.getConfig()));
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        datasetMapper.update(e);
        DatasetResponse r = new DatasetResponse();
        BeanMapper.mapper(e, r);
        r.setConfig(ObjectMapperUtils.fromJson(e.getConfig(), DataSetConfig.class));
        return r;
    }

    public boolean deleteDataset(String id) {
        DatasetEntity e = datasetMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);

        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return datasetMapper.update(e) > 0;
    }

    public DatasetResponse getDataset(String id) {
        DatasetEntity e = datasetMapper.selectOneById(id);
        if (e == null) {
            return null;
        }

        DatasetResponse r = new DatasetResponse();
        BeanMapper.mapper(e, r);
        r.setConfig(ObjectMapperUtils.fromJson(e.getConfig(), DataSetConfig.class));
        return r;
    }

    public PageResponse<DatasetResponse> listDatasets(DatasetQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", TenantContext.getTenantId())
                .and("is_deleted = 0");
        if (req.getFolderId() != null && !req.getFolderId().isBlank()) {
            // Find all sub-folders
            QueryWrapper folderQw = QueryWrapper.create()
                    .select("id")
                    .where("id = ?", req.getFolderId())
                    .or("ancestor_ids LIKE ?", "%" + req.getFolderId() + ",%");
            List<String> folderIds = folderMapper.selectObjectListByQueryAs(folderQw, String.class);

            if (folderIds != null && !folderIds.isEmpty()) {
                qw.in(DatasetEntity::getFolderId, folderIds);
            } else {
                // If no folder found (shouldn't happen if ID is valid), ensure no result
                qw.and("1 = 0");
            }
        }
        if (req.getDatasourceId() != null && !req.getDatasourceId().isBlank()) {
            qw.and("datasource_id = ?", req.getDatasourceId());
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name like ?", "%" + req.getName() + "%");
        }
        if (req.getType() != null) {
            qw.and("type = ?", req.getType());
        }
        if (req.getStatus() != null) {
            qw.and("status = ?", req.getStatus());
        }
        return PageHelper.apply(datasetMapper, qw, req, e -> {
            DatasetResponse r = new DatasetResponse();
            BeanMapper.mapper(e, r);
            r.setConfig(ObjectMapperUtils.fromJson(e.getConfig(), DataSetConfig.class));
            return r;
        });
    }

    public GenerateSqlResponse generateSql(GenerateSqlRequest req) {
        if (StringUtils.isAnyBlank(req.getDatasourceId(), req.getPrompt()) || req.getAgentId() == null) {
            return null;
        }
        DataSourceResponse ds = dataSourceService.get(req.getDatasourceId());
        if (ds == null) {
            return null;
        }
        String sql = ChatBiClient.generateSql(req.getAgentId(), ds.getUrl(), ds.getPassword(), req.getPrompt());

        PromptProperties.Prompt prompt = promptProperties.getPromptMap().get("generate-sql-output");

        String systemPrompt = prompt.getTemplate().replace("{{sql}}", sql);

        // String message = ChatProxy.applyTemplate(prompt.getTemplate(), Map.of("sql",
        // sql));
        // String response = ChatProxy.doChat(prompt.getModel(), message);

        // GenerateSqlResponse result = ChatProxy.parseMessage(response,
        // GenerateSqlResponse.class);

        GenerateSqlResponse result = agentScopeProxy.doChat(prompt.getName(), prompt.getModel(), systemPrompt,
                Msg.builder().textContent("从 SQL 查询结果抽象出数据结构，并给出稳定的数据转换脚本").build(), GenerateSqlResponse.class);
        result.setSql(sql);
        return result;
    }

    public DatasetResponse assistantChat(DataSetAssistantChatRequest req) {
        String dbId = req.getDbId();

        Map<String, Object> variables = new HashMap<>();
        variables.put("requirement", "<requirement><title>%s</title><description>%s</description></requirement>"
                .formatted(req.getTitle(), req.getDescription()));

        variables.put("data_target_schema", DatasetTargetSchemaGeneratorProxy.generate(req.getSchemaItem()));

        if (StringUtils.startsWith(dbId, "page-excel:")) {
            dbId = dbId.substring("page-excel:".length());
            try (CachedExcelDatabase excelDatabase = pageService.loadExcelDatabase(dbId)) {
                if (excelDatabase == null) {
                    return null;
                }
                List<ExcelSchema> excelSchemas = excelDatabase.getExcelSchemas();
                StringJoiner dbJoiner = new StringJoiner("",
                        "<database type=\"%s\" name=\"%s\" comment=\"%s\" current-datetime=\"%s\">".formatted("h2",
                                "public",
                                defaultEmpty("临时数据库"),
                                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)),
                        "</database>");
                excelSchemas.stream()
                        .flatMap(excel -> excel.getSheetSchemas().stream())
                        .forEach(table -> {
                            StringJoiner tableJoiner = new StringJoiner("",
                                    "<table name=\"%s\" comment=\"%s\">"
                                            .formatted(table.getTableName(), defaultEmpty(table.getSheetName())),
                                    "</table>");
                            table.getColumnMapping().forEach((rowTitle, columnName) -> {
                                tableJoiner.add("<column name=\"%s\" comment=\"%s\"/>"
                                        .formatted(columnName, defaultEmpty(rowTitle)));
                            });
                            dbJoiner.add(tableJoiner.toString());
                        });
                variables.put("db", dbJoiner.toString());

                PromptProperties.Prompt createDataSetPrompt = promptProperties.getPromptMap().get("create-dataset");

                // String requestMessage =
                // ChatProxy.applyTemplate(createDataSetPrompt.getTemplate(), variables);
                // DatasetResponse response =
                // ChatProxy.doChatWithRetry(createDataSetPrompt.getModel(), requestMessage,
                // DatasetResponse.class);
                AtomicReference<String> systemPrompt = new AtomicReference<>(createDataSetPrompt.getTemplate());
                variables.forEach((key, value) -> systemPrompt
                        .set(systemPrompt.get().replace("{{" + key + "}}", value.toString())));
                DatasetResponse response = agentScopeProxy.doChat(createDataSetPrompt.getName(), createDataSetPrompt.getModel(), systemPrompt.get(),
                        Msg.builder().textContent("生成数据集").build(), DatasetResponse.class);
                response.setDatasourceId(req.getDbId());
                response.setName(req.getTitle());
                response.setDescription(req.getDescription());
                response.getConfig().getSql().setDataSourceId(req.getDbId());
                response.setId(null);
                return response;
            }
        } else {
            DatasourceDbEntity db = dataSourceService.getDb(dbId);
            if (db == null) {
                return null;
            }
            List<DatasourceTableEntity> tables = dataSourceService.activeListTables(List.of(dbId));
            if (tables == null || tables.isEmpty()) {
                return null;
            }
            List<DatasourceFieldEntity> fields = dataSourceService.activeListFields(List.of(dbId));
            if (fields == null || fields.isEmpty()) {
                return null;
            }
            Map<String, List<DatasourceFieldEntity>> fieldMap = fields.stream()
                    .collect(Collectors.groupingBy(DatasourceFieldEntity::getTableId));

            StringJoiner dbJoiner = new StringJoiner("",
                    "<database type=\"%s\" name=\"%s\" comment=\"%s\" current-datetime=\"%s\">".formatted(db.getType(),
                            db.getName(),
                            defaultEmpty(db.getDescription()),
                            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)),
                    "</database>");
            tables.stream()
                    .filter(table -> fieldMap.containsKey(table.getId()))
                    .forEach(table -> {
                        StringJoiner tableJoiner = new StringJoiner("",
                                "<table name=\"%s\" comment=\"%s\">"
                                        .formatted(table.getName(), defaultEmpty(table.getDescription())),
                                "</table>");
                        fieldMap.get(table.getId()).forEach(field -> {
                            tableJoiner.add("<column name=\"%s\" comment=\"%s\"/>"
                                    .formatted(field.getName(), defaultEmpty(field.getDescription())));
                        });
                        dbJoiner.add(tableJoiner.toString());
                    });
            variables.put("db", dbJoiner.toString());

            PromptProperties.Prompt createDataSetPrompt = promptProperties.getPromptMap().get("create-dataset");

            // String requestMessage = ChatProxy.applyTemplate(createDataSetPrompt.getTemplate(), variables);
            // DatasetResponse response = ChatProxy.doChatWithRetry(createDataSetPrompt.getModel(), requestMessage,
            //         DatasetResponse.class);
            AtomicReference<String> systemPrompt = new AtomicReference<>(createDataSetPrompt.getTemplate());
            variables.forEach((key, value) -> systemPrompt
                    .set(systemPrompt.get().replace("{{" + key + "}}", value.toString())));
            DatasetResponse response = agentScopeProxy.doChat(createDataSetPrompt.getName(), createDataSetPrompt.getModel(), systemPrompt.get(),
                    Msg.builder().textContent("生成数据集").build(), DatasetResponse.class);
            response.setDatasourceId(db.getDatasourceId());
            response.setName(req.getTitle());
            response.setDescription(req.getDescription());
            response.getConfig().getSql().setDataSourceId(db.getDatasourceId());
            response.setId(null);
            return response;
        }
    }

    public List<Map<String, Object>> testSql(TestSqlRequest req) {
        try {
            HikariDataSource dataSource = getDataSource(req.getDbId());

            BoundingSql sql = MybatisUtils.compile(req.getSql(), Map.of());

            Statement statement = CCJSqlParserUtil.parse(sql.getSql());
            if (!(statement instanceof Select select)) {
                throw new BizException("测试SQL必须是SELECT语句");
            }
            Limit limit = Objects.requireNonNullElse(select.getLimit(), new Limit());

            if (limit.getRowCount() == null) {
                limit.setRowCount(new LongValue(10));
            } else if (limit.getRowCount() instanceof LongValue longValue) {
                limit.setRowCount(new LongValue(Math.min(longValue.getValue(), 10L)));
            }
            select.setLimit(limit);

            return MybatisUtils.queryForList(dataSource, select.toString(), Map.of());
        } catch (Exception e) {
            switch (e) {
                case JSQLParserException jsqlParserException: {

                    String message = jsqlParserException.getCause().getMessage();

                    if (message.startsWith("net.sf.jsqlparser.parser.ParseException: ")) {
                        message = message.substring("net.sf.jsqlparser.parser.ParseException: ".length());
                    }

                    throw new BizException(message, jsqlParserException.getCause());
                }
                case PersistenceException persistenceException: {
                    String message = persistenceException.getCause().getMessage();

                    throw new BizException(message, persistenceException.getCause());
                }
                default:
                    break;
            }
            throw new BizException("测试SQL执行失败", e);
        }
    }

    public DataSetExeResponse execute(String id, String aiPrompt, Map<String, Object> params) {
        DatasetResponse dataset = this.getDataset(id);

        Map<String, Object> variables = new HashMap<>();
        Input input = dataset.getConfig().getInput();
        if (input != null && input.getFields() != null) {
            input.getFields().forEach(field -> {
                variables.put(field.getKey(), params.getOrDefault(field.getKey(), ""));
            });
        }

        Object data = null;
        switch (dataset.getType()) {
            case SQL:
                Sql sql = dataset.getConfig().getSql();
                HikariDataSource dataSource = getDataSource(sql.getDataSourceId());
                data = MybatisUtils.queryForList(dataSource, sql.getSql(), variables);
                break;
            case API:
                data = callApi(dataset.getConfig().getApi(), variables);
                break;
            default:
                break;
        }

        String script = dataset.getConfig().getScript();
        if (StringUtils.isNotBlank(script)) {
            data = GraalJsUtil.execute("function nomalize(data) { %s }".formatted(script), "nomalize", data);
        }

        DataSetExeResponse res = new DataSetExeResponse();
        res.setResult(data);

        if (StringUtils.isNotBlank(aiPrompt)) {
            PromptProperties.Prompt prompt = promptProperties.getPromptMap().get("dataset-result-explain");

            String systemPrompt = prompt.getTemplate().replace("{{dataset_result}}", ObjectMapperUtils.toJson(data));
            String explain = agentScopeProxy.doChat(prompt.getName(), prompt.getModel(), systemPrompt, Msg.builder().textContent(aiPrompt).build(), String.class);
            res.setExplain(explain);
        }

        return res;
    }

    private HikariDataSource getDataSource(String dataSourceId) {
        DataSource dataSource = null;
        if (StringUtils.startsWith(dataSourceId, "page-excel:")) {
            CachedExcelDatabase excelDatabase = pageService
                    .loadExcelDatabase(dataSourceId.substring("page-excel:".length()));
            if (excelDatabase != null) {
                dataSource = excelDatabase.getDataSource();
            }
        } else {
            dataSource = dataSourceManager.getDataSource(dataSourceId);
        }
        if (dataSource == null) {
            throw new BizException("数据源不存在");
        }
        return (HikariDataSource) dataSource;
    }

    private Object callApi(DataSetConfig.Api api, Map<String, Object> variables) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            String url = api.getUrl();
            StringJoiner queryString = new StringJoiner("&");

            Optional.ofNullable(api.getParams()).ifPresent(params -> {
                params.forEach((key, value) -> {
                    queryString.add("%s=%s".formatted(key, SpringElUtil.evaluateExpression(value, variables)));
                });
            });
            if (StringUtils.isNotBlank(queryString.toString())) {
                url += url.contains("?") ? "&" + queryString.toString() : "?" + queryString.toString();
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method(api.getMethod(),
                            StringUtils.equalsAny(api.getMethod(), "POST", "PUT")
                                    && StringUtils.isNotBlank(api.getBody())
                                            ? BodyPublishers.ofString(api.getBody(), StandardCharsets.UTF_8)
                                            : null)
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            Optional.ofNullable(api.getHeaders()).ifPresent(headers -> {
                headers.forEach((key, value) -> {
                    requestBuilder
                            .header(key, SpringElUtil.evaluateExpression(value, variables, String.class));
                });
            });
            HttpResponse<String> resp = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new BizException("调用API失败，URL：%s，状态码：%d".formatted(url, resp.statusCode()));
            }
            String body = resp.body();
            return JSON.parse(body);
        } catch (Exception e) {
            throw new BizException("调用API失败", e);
        }
    }

    private String defaultEmpty(String value) {
        return value != null ? value : "";
    }
}
