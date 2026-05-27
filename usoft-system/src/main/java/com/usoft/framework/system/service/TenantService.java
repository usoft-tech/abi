package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.system.api.TenantCreateRequest;
import com.usoft.framework.system.api.TenantQueryRequest;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.api.TenantSettingRequest;
import com.usoft.framework.system.api.TenantUpdateRequest;
import com.usoft.framework.system.api.TenantUserResponse;
import com.usoft.framework.system.api.UserQueryRequest;
import com.usoft.framework.system.api.dto.TenantSiteConfig;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.entity.TenantEntity;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.entity.UserTenantEntity;
import com.usoft.framework.system.mapper.TenantMapper;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.system.mapper.UserTenantMapper;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.exception.BizException;

@Service
public class TenantService {
    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final UserTenantMapper userTenantMapper;

    public TenantService(TenantMapper tenantMapper, UserMapper userMapper, UserTenantMapper userTenantMapper) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.userTenantMapper = userTenantMapper;
    }

    public TenantResponse create(TenantCreateRequest req) {
        TenantEntity e = new TenantEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        tenantMapper.insert(e);
        TenantResponse r = new TenantResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    public TenantResponse update(String id, TenantUpdateRequest req) {
        TenantEntity e = tenantMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        BeanMapper.mapper(req, e);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        tenantMapper.update(e);
        TenantResponse r = new TenantResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    public boolean delete(String id) {
        TenantEntity e = tenantMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return tenantMapper.update(e) > 0;
    }

    public TenantResponse get(String id) {
        TenantEntity e = tenantMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        TenantResponse r = new TenantResponse();
        BeanMapper.mapper(e, r);
        r.setSiteConfig(ObjectMapperUtils.fromJson(e.getSiteConfig(), TenantSiteConfig.class));
        return r;
    }

    public PageResponse<TenantResponse> list(TenantQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getCode() != null && !req.getCode().isBlank()) {
            qw.and("code LIKE ?", "%" + req.getCode() + "%");
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getStatus() != null) {
            qw.and("status = ?", req.getStatus());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(code LIKE ? OR name LIKE ?)", kw, kw);
        }
        return PageHelper.apply(tenantMapper, qw, req, e -> {
            TenantResponse r = new TenantResponse();
            BeanMapper.mapper(e, r);
            r.setSiteConfig(ObjectMapperUtils.fromJson(e.getSiteConfig(), TenantSiteConfig.class));
            return r;
        });
    }

    public PageResponse<TenantUserResponse> listTenantUsers(String tenantId, UserQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*, ut.auth_key")
                .from(UserEntity.class).as("u")
                .join(UserTenantEntity.class).as("ut").on("ut.user_id = u.id")
                .where("ut.tenant_id = ?", tenantId)
                .and("(u.is_deleted = 0 OR u.is_deleted IS NULL)");

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            qw.and("u.username LIKE ?", "%" + req.getUsername() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(u.username LIKE ? OR u.display_name LIKE ?)", kw, kw);
        }

        return PageHelper.apply(userMapper, qw, req, TenantUserResponse.class);
    }

    public void addTenantUsers(String tenantId, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (String userId : userIds) {
            // Check if already exists
            long count = userTenantMapper.selectCountByQuery(QueryWrapper.create()
                    .where("user_id = ?", userId)
                    .and("tenant_id = ?", tenantId));
            if (count > 0) {
                continue;
            }
            UserTenantEntity e = new UserTenantEntity();
            e.setTenantId(tenantId);
            e.setUserId(userId);
            e.setAuthKey(TenantAuthKey.USER);
            userTenantMapper.insert(e);
        }
    }

    public void removeTenantUser(String tenantId, String userId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("user_id = ?", userId);
        userTenantMapper.deleteByQuery(qw);
    }

    public TenantResponse setting(String tenantId, TenantSettingRequest req) {
        TenantEntity e = tenantMapper.selectOneById(tenantId);
        if (e == null) {
            return null;
        }
        BeanMapper.mapper(req, e);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        e.setSiteConfig(ObjectMapperUtils.toJson(req.getSiteConfig()));
        tenantMapper.update(e);
        TenantResponse r = new TenantResponse();
        BeanMapper.mapper(e, r);
        r.setSiteConfig(req.getSiteConfig());
        return r;
    }

    public void changeTenantUserAuthKey(String tenantId, String userId, TenantAuthKey authKey) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("user_id = ?", userId);
        UserTenantEntity e = new UserTenantEntity();
        e.setAuthKey(authKey);
        userTenantMapper.updateByQuery(e, qw);
    }

    /**
     * 检查用户是否是租户的指定权限
     * 
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param authKey  权限
     */
    public void checkTenantUserAuthKey(String tenantId, String userId, TenantAuthKey authKey) {
        if (StringUtils.equals(UserHolder.userId(), "1")) {
            return;
        }
        List<UserTenantEntity> list = userTenantMapper.selectListByQuery(QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("user_id = ?", userId));
        if (list.isEmpty()) {
            throw new BizException("用户不是该租户的" + authKey.getLabel());
        }
        if (authKey == TenantAuthKey.ADMIN && list.stream().noneMatch(ut -> ut.getAuthKey() == TenantAuthKey.ADMIN)) {
            throw new BizException("用户不是该租户的" + authKey.getLabel());
        }
    }

    /**
     * 检查用户是否是租户的指定权限
     * 
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param authKey  权限
     */
    public boolean isTenantUserAuthKey(String tenantId, String userId, TenantAuthKey authKey) {
        if (StringUtils.equals(UserHolder.userId(), "1")) {
            return true;
        }
        List<UserTenantEntity> list = userTenantMapper.selectListByQuery(QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("user_id = ?", userId));
        if (list.isEmpty()) {
            return false;
        }
        if (authKey == TenantAuthKey.ADMIN && list.stream().noneMatch(ut -> ut.getAuthKey() == TenantAuthKey.ADMIN)) {
            return false;
        }
        return true;
    }
}
