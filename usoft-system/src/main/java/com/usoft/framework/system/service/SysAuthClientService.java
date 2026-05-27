package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.SysAuthClientCreateRequest;
import com.usoft.framework.system.api.SysAuthClientQueryRequest;
import com.usoft.framework.system.api.SysAuthClientResponse;
import com.usoft.framework.system.api.SysAuthClientUpdateRequest;
import com.usoft.framework.system.entity.SysAuthClientEntity;
import com.usoft.framework.system.mapper.SysAuthClientMapper;

/**
 * 客户端凭证服务
 */
@Service
public class SysAuthClientService {

    private final SysAuthClientMapper sysAuthClientMapper;

    /**
     * 构造服务
     */
    public SysAuthClientService(SysAuthClientMapper sysAuthClientMapper) {
        this.sysAuthClientMapper = sysAuthClientMapper;
    }

    /**
     * 创建客户端凭证
     */
    public SysAuthClientResponse create(SysAuthClientCreateRequest req) {
        String tenantId = TenantContext.getTenantId();
        SysAuthClientEntity e = new SysAuthClientEntity();
        e.setTenantId(tenantId);
        String rawClientId = UUID.randomUUID().toString().replace("-", "");
        String rawClientSecret = UUID.randomUUID().toString().replace("-", "");
        e.setClientId(rawClientId);
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setClientSecret(rawClientSecret);
        e.setExpiredAt(req.getExpiredAt());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        sysAuthClientMapper.insert(e);
        SysAuthClientResponse r = toResponse(e);
        r.setClientSecret(rawClientSecret);
        return r;
    }

    /**
     * 更新客户端凭证
     */
    public SysAuthClientResponse update(String clientId, SysAuthClientUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        SysAuthClientEntity e = sysAuthClientMapper.selectCurrentTenantClient(tenantId, clientId);
        if (e == null) {
            return null;
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            e.setName(req.getName());
        }
        if (req.getDescription() != null) {
            e.setDescription(req.getDescription());
        }
        if (req.getExpiredAt() != null) {
            e.setExpiredAt(req.getExpiredAt());
        }
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        sysAuthClientMapper.update(e);
        
        return toResponse(e);
    }

    /**
     * 删除客户端凭证
     */
    public boolean delete(String clientId) {
        String tenantId = TenantContext.getTenantId();
        SysAuthClientEntity e = sysAuthClientMapper.selectCurrentTenantClient(tenantId, clientId);
        if (e == null) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return sysAuthClientMapper.update(e) > 0;
    }

    /**
     * 查询客户端凭证详情
     */
    public SysAuthClientResponse get(String clientId) {
        String tenantId = TenantContext.getTenantId();
        SysAuthClientEntity e = sysAuthClientMapper.selectCurrentTenantClient(tenantId, clientId);
        if (e == null) {
            return null;
        }
        if (e.getIsDeleted() != null && e.getIsDeleted()) {
            return null;
        }
        
        return toResponse(e);
    }

    /**
     * 分页查询当前租户的客户端凭证
     */
    public PageResponse<SysAuthClientResponse> list(SysAuthClientQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getClientId() != null && !req.getClientId().isBlank()) {
            qw.and("client_id LIKE ?", "%" + req.getClientId() + "%");
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(client_id LIKE ? OR name LIKE ? OR description LIKE ?)", kw, kw, kw);
        }
        return PageHelper.apply(sysAuthClientMapper, qw, req, this::toResponse);
    }

    /**
     * 刷新客户端密钥
     */
    public SysAuthClientResponse refreshSecret(String clientId) {
        String tenantId = TenantContext.getTenantId();
        SysAuthClientEntity e = sysAuthClientMapper.selectCurrentTenantClient(tenantId, clientId);
        if (e == null) {
            return null;
        }
        if (e.getIsDeleted() != null && e.getIsDeleted()) {
            return null;
        }
        String rawClientSecret = UUID.randomUUID().toString().replace("-", "");
        e.setClientSecret(rawClientSecret);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        sysAuthClientMapper.update(e);
        
        SysAuthClientResponse r = toResponse(e);
        r.setClientSecret(rawClientSecret);
        return r;
    }

    /**
     * 验证客户端凭证
     */
    public SysAuthClientResponse validateClient(String clientId, String clientSecret) {
        QueryWrapper qw = QueryWrapper.create()
                .where("client_id = ?", clientId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        SysAuthClientEntity e = sysAuthClientMapper.selectOneByQuery(qw);
        
        if (e == null) {
            return null;
        }
        
        if (!e.getClientSecret().equals(clientSecret)) {
            return null;
        }
        
        if (e.getExpiredAt() != null && e.getExpiredAt().isBefore(Instant.now())) {
            return null;
        }
        
        return toResponse(e);
    }

    private SysAuthClientResponse toResponse(SysAuthClientEntity e) {
        SysAuthClientResponse r = new SysAuthClientResponse();
        r.setTenantId(e.getTenantId());
        r.setClientId(e.getClientId());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setClientSecret("********");
        r.setExpiredAt(e.getExpiredAt());
        return r;
    }
}
