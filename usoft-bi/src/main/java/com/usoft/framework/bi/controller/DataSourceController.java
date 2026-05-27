package com.usoft.framework.bi.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;
import com.usoft.framework.common.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.bi.api.ChatBiAgentResponse;
import com.usoft.framework.bi.api.DataSourceCreateRequest;
import com.usoft.framework.bi.api.DataSourceQueryRequest;
import com.usoft.framework.bi.api.DataSourceResponse;
import com.usoft.framework.bi.api.DataSourceUpdateRequest;
import com.usoft.framework.bi.api.DatasourceDescriptionUpdateRequest;
import com.usoft.framework.bi.api.DatasourceFieldQueryRequest;
import com.usoft.framework.bi.api.DatasourceStatusUpdateRequest;
import com.usoft.framework.bi.db.adaptor.DbAdaptorFactory;
import com.usoft.framework.bi.entity.DatasourceDbEntity;
import com.usoft.framework.bi.entity.DatasourceFieldEntity;
import com.usoft.framework.bi.entity.DatasourceTableEntity;
import com.usoft.framework.bi.service.DataSourceService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.enums.EnableStatus;

@RestController
@RequestMapping(value = "/api/bi/datasources", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "数据源管理", authorize = "tenant:sa:datasource", group = true),
})
public class DataSourceController {
    private final DataSourceService dataSourceService;
    private final StringRedisTemplate redisTemplate;

    public DataSourceController(DataSourceService dataSourceService, StringRedisTemplate redisTemplate) {
        this.dataSourceService = dataSourceService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    @AuthorizeDescription("创建数据源")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:create')")
    public ApiResponse<DataSourceResponse> create(@Validated @RequestBody DataSourceCreateRequest req) {
        return ApiResponse.ok(dataSourceService.create(req));
    }

    @PutMapping("/{id}")
    @AuthorizeDescription("更新数据源")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<DataSourceResponse> update(@PathVariable String id,
            @Validated @RequestBody DataSourceUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除数据源")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(dataSourceService.delete(id));
    }

    @GetMapping("/{id}")
    @AuthorizeDescription("获取数据源详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:get')")
    public ApiResponse<DataSourceResponse> get(@PathVariable String id) {
        return ApiResponse.ok(dataSourceService.get(id));
    }

    @GetMapping
    @AuthorizeDescription("分页查询数据源")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:list')")
    public ApiResponse<PageResponse<DataSourceResponse>> list(@Validated DataSourceQueryRequest req) {
        return ApiResponse.ok(dataSourceService.list(req));
    }

    @PostMapping({
            "/test-connection/",
            "/test-connection/{id}"
    })
    @AuthorizeDescription("测试数据源连接")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:test-connection')")
    public ApiResponse<Boolean> testConnection(
            @PathVariable(required = false) String id,
            @Validated @RequestBody DataSourceCreateRequest req) {
        // 获取限流标识字段
        String type = req.getType();
        String host = req.getHost();
        Integer port = req.getPort();

        if (StringUtils.isBlank(type) || StringUtils.isBlank(host) || port == null) {
            if (StringUtils.isNotBlank(id)) {
                DataSourceResponse ds = dataSourceService.get(id);
                if (ds != null) {
                    type = StringUtils.isBlank(type) ? ds.getType() : type;
                    host = StringUtils.isBlank(host) ? ds.getHost() : host;
                    port = port == null ? ds.getPort() : port;
                }
            }
        }

        // 构造 Redis Key
        String keySuffix = String.format("%s:%s:%d", type, host, port);
        String limitKey = "bi:ds:test:limit:" + keySuffix;
        String failKey = "bi:ds:test:fail:" + keySuffix + ":" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // 1. 检查是否已被锁定（当天失败5次）
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr == null ? 0 : Integer.parseInt(failCountStr);
        if (failCount >= 5) {
            throw new BizException("该数据源测试失败次数过多，今日已被锁定");
        }

        // 2. 检查30秒限流
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(absent)) {
            throw new BizException("测试连接过于频繁，请30秒后再试");
        }

        if (StringUtils.isBlank(req.getPassword()) && StringUtils.isNotBlank(id)) {
            req.setPassword(dataSourceService.get(id).getPassword());
        }

        boolean testConnection = false;
        try {
            testConnection = DbAdaptorFactory.getDbAdaptor(req.getType(), id).testConnection(req);
        } catch (Exception e) {
            // 记录失败次数
            incrementFailCount(failKey);
            throw e;
        }

        if (!testConnection) {
            incrementFailCount(failKey);
        }

        return ApiResponse.ok(testConnection);
    }

    /**
     * 增加失败计数并设置过期时间
     * @param failKey Redis Key
     */
    private void incrementFailCount(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, Duration.ofDays(1));
        }
    }

    @PutMapping("/{id}/sync-schema")
    @AuthorizeDescription("同步数据源结构")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> syncSchema(@PathVariable String id) {
        dataSourceService.syncSchema(id);
        return ApiResponse.ok(true);
    }

    @GetMapping("/dbs")
    @AuthorizeDescription("查询数据库列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:get')")
    public ApiResponse<List<DatasourceDbEntity>> listDbs(@RequestParam String datasourceId) {
        return ApiResponse.ok(dataSourceService.listDbs(datasourceId));
    }

    @GetMapping("/tables")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:get')")
    public ApiResponse<List<DatasourceTableEntity>> listTables(@RequestParam String dbId) {
        return ApiResponse.ok(dataSourceService.listTables(dbId));
    }

    @GetMapping("/agents")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:get')")
    public ApiResponse<List<ChatBiAgentResponse>> listAgents(DataSourceQueryRequest req) {
        req.setSize(1000);
        req.setPage(1);
        List<DataSourceResponse> dsList = dataSourceService.list(req).getItems();
        List<ChatBiAgentResponse> result = new ArrayList<>();
        dsList.forEach(ds -> {
            List<DatasourceDbEntity> listDbs = dataSourceService.listDbs(ds.getId());
            ChatBiAgentResponse group = ChatBiAgentResponse.builder()
                    .id(ds.getId())
                    .type(ds.getType())
                    .name(ds.getName())
                    .group(true)
                    .build();
            result.add(group);
            group.setChildren(listDbs.stream()
                    .filter(db -> db.getStatus() == EnableStatus.ENABLE)
                    .map(db -> ChatBiAgentResponse.builder()
                            .id(db.getId())
                            .name(db.getName())
                            .group(false)
                            .build())
                    .toList());
        });
        return ApiResponse.ok(result);
    }

    @GetMapping("/fields")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:get')")
    public ApiResponse<PageResponse<DatasourceFieldEntity>> pageFields(@Validated DatasourceFieldQueryRequest req) {
        return ApiResponse.ok(dataSourceService.pageFields(req));
    }

    @PutMapping("/dbs/{id}/status")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateDbStatus(@PathVariable String id,
            @RequestBody DatasourceStatusUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateDbStatus(id, req.getStatus()));
    }

    @PutMapping("/tables/{id}/status")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateTableStatus(@PathVariable String id,
            @RequestBody DatasourceStatusUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateTableStatus(id, req.getStatus()));
    }

    @PutMapping("/fields/{id}/status")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateFieldStatus(@PathVariable String id,
            @RequestBody DatasourceStatusUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateFieldStatus(id, req.getStatus()));
    }

    @PutMapping("/dbs/{id}/description")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateDbDescription(@PathVariable String id,
            @RequestBody DatasourceDescriptionUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateDbDescription(id, req.getDescription()));
    }

    @PutMapping("/tables/{id}/description")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateTableDescription(@PathVariable String id,
            @RequestBody DatasourceDescriptionUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateTableDescription(id, req.getDescription()));
    }

    @PutMapping("/fields/{id}/description")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:datasource:update')")
    public ApiResponse<Boolean> updateFieldDescription(@PathVariable String id,
            @RequestBody DatasourceDescriptionUpdateRequest req) {
        return ApiResponse.ok(dataSourceService.updateFieldDescription(id, req.getDescription()));
    }
}
