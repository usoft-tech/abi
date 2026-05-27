package com.usoft.framework.bi.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.BatchUpdateAuthRequest;
import com.usoft.framework.bi.api.BatchUpdateAuthRequest.AuthItem;
import com.usoft.framework.bi.api.enums.AuthorizationScope;
import com.usoft.framework.bi.entity.AuthorizationEntity;
import com.usoft.framework.bi.mapper.AuthorizationMapper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * 授权服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final AuthorizationMapper authorizationMapper;

    @Transactional
    public AuthorizationEntity save(AuthorizationEntity entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
        }
        if (entity.getTenantId() == null) {
            entity.setTenantId(TenantContext.getTenantId());
        }
        entity.setIsDeleted(false);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(UserHolder.username());
        authorizationMapper.insert(entity);
        return entity;
    }

    @Transactional
    public boolean update(AuthorizationEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        return authorizationMapper.update(entity) > 0;
    }

    @Transactional
    public boolean delete(String id) {
        AuthorizationEntity entity = authorizationMapper.selectOneById(id);
        if (entity == null) {
            return false;
        }
        entity.setIsDeleted(true);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        return authorizationMapper.update(entity) > 0;
    }

    public AuthorizationEntity get(String id) {
        return authorizationMapper.selectOneById(id);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void update(BatchUpdateAuthRequest req) {
        // 1. 查询原有授权
        List<AuthorizationEntity> oldList = listByBizId(req.getBizType(), req.getBizId());
        
        // 2. 删除原有授权
        for (AuthorizationEntity entity : oldList) {
            delete(entity.getId());
        }

        // 3. 保存新授权
        if (req.getAuthorizations() != null && !req.getAuthorizations().isEmpty()) {
            for (AuthItem item : req.getAuthorizations()) {
                AuthorizationEntity entity = new AuthorizationEntity();
                entity.setBizType(req.getBizType());
                entity.setBizId(req.getBizId());
                entity.setAuthorizerScope(item.getScope());
                entity.setAuthorizerId(item.getAuthorizerId());
                save(entity);
            }
        }
    }

    public List<AuthorizationEntity> listByBizId(BizType bizType, String bizId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("biz_type = ?", bizType)
                .and("biz_id = ?", bizId)
                .and("is_deleted = 0")
                .and("tenant_id = ?", TenantContext.getTenantId());
        return authorizationMapper.selectListByQuery(qw);
    }

    public void checkAuth(String tenantId, BizType bizType, String bizId, String userId) {
        QueryWrapper qw = QueryWrapper.create()
                .from(AuthorizationEntity.class).as("a")
                .where("is_deleted = 0")
                .eq("biz_type", bizType)
                .eq("biz_id", bizId)
                .eq("tenant_id", tenantId, StringUtils.isNotBlank(tenantId))
                .in("authorizer_scope", List.of(AuthorizationScope.PUBLIC, AuthorizationScope.TENANT))
                .unionAll(
                        QueryWrapper.create()
                                .from(AuthorizationEntity.class).as("b")
                                .where("is_deleted = 0")
                                .eq("biz_type", bizType)
                                .eq("biz_id", bizId)
                                .eq("tenant_id", tenantId, StringUtils.isNotBlank(tenantId))
                                .eq("authorizer_scope", AuthorizationScope.USER)
                                .eq("authorizer_id", Objects.requireNonNullElse(userId, "-1")));
        if (authorizationMapper.selectCountByQuery(QueryWrapper.create().from(qw).as("t")) == 0) {
            throw new AccessDeniedException("无权限访问");
        }
    }

    public void delete(BizType bizType, String bizId) {
         QueryWrapper qw = QueryWrapper.create()
                .where("biz_type = ?", bizType)
                .and("biz_id = ?", bizId)
                .and("is_deleted = 0")
                .and("tenant_id = ?", TenantContext.getTenantId());
        AuthorizationEntity entity = new AuthorizationEntity();
        entity.setIsDeleted(true);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        authorizationMapper.updateByQuery(entity, qw);
    }
}
