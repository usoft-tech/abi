package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.SysConfigCreateRequest;
import com.usoft.framework.system.api.SysConfigQueryRequest;
import com.usoft.framework.system.api.SysConfigResponse;
import com.usoft.framework.system.api.SysConfigUpdateRequest;
import com.usoft.framework.system.entity.SysConfigEntity;
import com.usoft.framework.system.mapper.SysConfigMapper;
import com.usoft.framework.common.api.PageResponse;

/**
 * 系统配置服务
 */
@Service
public class SysConfigService {
    private final SysConfigMapper sysConfigMapper;

    /**
     * 构造服务
     */
    public SysConfigService(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    /**
     * 创建系统配置
     */
    // @CacheEvict(value = {"sysConfig","sysConfigs"}, allEntries = true)
    public SysConfigResponse create(SysConfigCreateRequest req) {
        SysConfigEntity e = new SysConfigEntity();
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setConfigKey(req.getConfigKey());
        e.setConfigValue(req.getConfigValue());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        sysConfigMapper.insert(e);
        SysConfigResponse r = new SysConfigResponse();
        r.setId(e.getId());
        r.setConfigKey(e.getConfigKey());
        r.setConfigValue(e.getConfigValue());
        return r;
    }

    /**
     * 更新系统配置
     */
    // @CacheEvict(value = {"sysConfig","sysConfigs"}, allEntries = true)
    public SysConfigResponse update(String id, SysConfigUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        SysConfigEntity e = sysConfigMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        e.setConfigKey(req.getConfigKey());
        e.setConfigValue(req.getConfigValue());
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        sysConfigMapper.update(e);
        SysConfigResponse r = new SysConfigResponse();
        r.setId(e.getId());
        r.setConfigKey(e.getConfigKey());
        r.setConfigValue(e.getConfigValue());
        return r;
    }

    /**
     * 删除系统配置
     */
    // @CacheEvict(value = {"sysConfig","sysConfigs"}, allEntries = true)
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        SysConfigEntity e = sysConfigMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return sysConfigMapper.update(e) > 0;
    }

    /**
     * 查询系统配置详情
     */
    public SysConfigResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        SysConfigEntity e = sysConfigMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        SysConfigResponse r = new SysConfigResponse();
        r.setId(e.getId());
        r.setConfigKey(e.getConfigKey());
        r.setConfigValue(e.getConfigValue());
        return r;
    }

    /**
     * 列出当前租户的系统配置
     */
    public PageResponse<SysConfigResponse> list(SysConfigQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getConfigKey() != null && !req.getConfigKey().isBlank()) {
            qw.and("config_key LIKE ?", "%" + req.getConfigKey() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(config_key LIKE ? OR config_value LIKE ?)", kw, kw);
        }
        return PageHelper.apply(sysConfigMapper, qw, req, e -> {
            SysConfigResponse r = new SysConfigResponse();
            r.setId(e.getId());
            r.setConfigKey(e.getConfigKey());
            r.setConfigValue(e.getConfigValue());
            return r;
        });
    }
    
}
