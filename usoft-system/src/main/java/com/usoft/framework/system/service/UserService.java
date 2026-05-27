package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.utils.PasswordUtils;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.PermissionResponse;
import com.usoft.framework.system.api.RoleResponse;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.api.TenantUserResponse;
import com.usoft.framework.system.api.UserCreateRequest;
import com.usoft.framework.system.api.UserQueryRequest;
import com.usoft.framework.system.api.UserResponse;
import com.usoft.framework.system.api.UserUpdateRequest;
import com.usoft.framework.system.api.dto.TenantSiteConfig;
import com.usoft.framework.system.entity.RoleEntity;
import com.usoft.framework.system.entity.SysPermissionEntity;
import com.usoft.framework.system.entity.SysRolePermissionEntity;
import com.usoft.framework.system.entity.SysUserRoleEntity;
import com.usoft.framework.system.entity.TenantEntity;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.entity.UserTenantEntity;
import com.usoft.framework.system.mapper.RoleMapper;
import com.usoft.framework.system.mapper.SysPermissionMapper;
import com.usoft.framework.system.mapper.TenantMapper;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.system.mapper.UserTenantMapper;
import com.usoft.framework.utils.ObjectMapperUtils;

/**
 * 用户服务
 */
@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserTenantMapper userTenantMapper;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;

    private final RoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    /**
     * 构造服务
     */
    public UserService(UserMapper userMapper, UserTenantMapper userTenantMapper, TenantMapper tenantMapper,
            PasswordEncoder passwordEncoder, RoleMapper roleMapper, SysPermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.userTenantMapper = userTenantMapper;
        this.tenantMapper = tenantMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 创建用户
     */
    public UserResponse create(UserCreateRequest req) {
        UserEntity e = new UserEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        PasswordUtils.validatePassword(req.getPassword(), e.getUsername());
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        userMapper.insert(e);

        // 关联租户
        UserTenantEntity ut = new UserTenantEntity();
        ut.setUserId(e.getId());
        ut.setTenantId(TenantContext.getTenantId());
        userTenantMapper.insert(ut);

        UserResponse r = new UserResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    /**
     * 更新用户
     */
    public UserResponse update(String id, UserUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        UserEntity e = userMapper.selectOneById(id);
        if (e == null) {
            return null;
        }

        // 检查关联
        QueryWrapper qw = QueryWrapper.create()
                .where(UserTenantEntity::getUserId).eq(id)
                .and(UserTenantEntity::getTenantId).eq(tenantId);
        if (userTenantMapper.selectCountByQuery(qw) == 0) {
            return null;
        }

        BeanMapper.mapper(req, e);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            PasswordUtils.validatePassword(req.getPassword(), e.getUsername());
            e.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        userMapper.update(e);
        UserResponse r = new UserResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    /**
     * 更新个人信息
     */
    public UserResponse updateProfile(String id, com.usoft.framework.system.api.UserProfileUpdateRequest req) {
        UserEntity e = userMapper.selectOneById(id);
        if (e == null) {
            return null;
        }

        if (req.getDisplayName() != null) {
            e.setDisplayName(req.getDisplayName());
        }
        if (req.getAvatar() != null) {
            e.setAvatar(req.getAvatar());
        }

        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (req.getOldPassword() == null || req.getOldPassword().isBlank()) {
                throw new com.usoft.framework.common.exception.BizException("修改密码需要提供旧密码");
            }
            if (!passwordEncoder.matches(req.getOldPassword(), e.getPassword())) {
                throw new com.usoft.framework.common.exception.BizException("旧密码不正确");
            }
            if (req.getNewPassword().length() < 8) {
                throw new com.usoft.framework.common.exception.BizException("新密码长度不能小于8位");
            }
            PasswordUtils.validatePassword(req.getNewPassword(), e.getUsername());
            e.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        userMapper.update(e);
        UserResponse r = new UserResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    /**
     * 删除用户
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        UserEntity e = userMapper.selectOneById(id);
        if (e == null) {
            return false;
        }

        // 检查关联
        QueryWrapper qw = QueryWrapper.create()
                .where(UserTenantEntity::getUserId).eq(id)
                .and(UserTenantEntity::getTenantId).eq(tenantId);
        if (userTenantMapper.selectCountByQuery(qw) == 0) {
            return false;
        }

        // 移除关联
        return userTenantMapper.deleteByQuery(qw) > 0;
    }

    /**
     * 查询用户详情
     */
    public UserResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        UserEntity e = userMapper.selectOneById(id);
        if (e == null) {
            return null;
        }

        // 检查关联
        QueryWrapper qw = QueryWrapper.create()
                .where(UserTenantEntity::getUserId).eq(id)
                .and(UserTenantEntity::getTenantId).eq(tenantId);
        if (userTenantMapper.selectCountByQuery(qw) == 0) {
            return null;
        }

        UserResponse r = new UserResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    /**
     * 列出当前租户的用户（分页筛选）
     */
    public PageResponse<UserResponse> list(UserQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*")
                .from(UserEntity.class).as("u")
                .and("(u.is_deleted = 0 OR u.is_deleted IS NULL)");
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            qw.and("u.username LIKE ?", "%" + req.getUsername() + "%");
        }
        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            qw.and("u.department_id = ?", req.getDepartmentId());
        }
        if (req.getEmployeeNo() != null && !req.getEmployeeNo().isBlank()) {
            qw.and("u.employee_no LIKE ?", "%" + req.getEmployeeNo() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(u.username LIKE ? OR u.employee_no LIKE ?)", kw, kw);
        }
        return PageHelper.apply(userMapper, qw, req, e -> {
            UserResponse r = new UserResponse();
            BeanMapper.mapper(e, r);
            return r;
        });
    }

    /**
     * 列出当前租户的用户（分页筛选）
     */
    public PageResponse<TenantUserResponse> listFromTenant(UserQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*, ut.auth_key")
                .from(UserEntity.class).as("u")
                .join(UserTenantEntity.class).as("ut").on("ut.user_id = u.id")
                .where("ut.tenant_id = ?", tenantId)
                .and("(u.is_deleted = 0 OR u.is_deleted IS NULL)");
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            qw.and("u.username LIKE ?", "%" + req.getUsername() + "%");
        }
        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            qw.and("u.department_id = ?", req.getDepartmentId());
        }
        if (req.getEmployeeNo() != null && !req.getEmployeeNo().isBlank()) {
            qw.and("u.employee_no LIKE ?", "%" + req.getEmployeeNo() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(u.username LIKE ? OR u.employee_no LIKE ?)", kw, kw);
        }
        return PageHelper.apply(userMapper, qw, req, TenantUserResponse.class);
    }

    /**
     * 根据用户ID获取租户列表
     */
    public List<TenantResponse> listTenantsByUserId(String userId) {
        QueryWrapper qw = QueryWrapper.create()
                .select("t.*")
                .from(TenantEntity.class).as("t")
                .join(UserTenantEntity.class).as("ut").on("ut.tenant_id = t.id")
                .where("ut.user_id = ?", userId)
                .and("(t.is_deleted = 0 OR t.is_deleted IS NULL)");
        List<TenantEntity> rows = tenantMapper.selectListByQuery(qw);
        return rows.stream().map(e -> {
            TenantResponse r = new TenantResponse();
            BeanMapper.mapper(e, r);
            r.setSiteConfig(ObjectMapperUtils.fromJson(e.getSiteConfig(), TenantSiteConfig.class));
            return r;
        }).collect(Collectors.toList());
    }

    public List<RoleResponse> listRoleByUserId(String id) {
        QueryWrapper qw = QueryWrapper.create()
                .select("r.*")
                .from(RoleEntity.class).as("r")
                .join(SysUserRoleEntity.class).as("ur").on("ur.role_id = r.id")
                .where("ur.user_id = ?", id)
                .and("(r.is_deleted = 0 OR r.is_deleted IS NULL)");
        List<RoleEntity> rows = roleMapper.selectListByQuery(qw);
        return rows.stream().map(e -> {
            RoleResponse r = new RoleResponse();
            BeanMapper.mapper(e, r);
            return r;
        }).collect(Collectors.toList());
    }

    public List<PermissionResponse> listPermissionByUserId(String id) {
        QueryWrapper qw = QueryWrapper.create()
                .select("p.*")
                .from(SysPermissionEntity.class).as("p")
                .join(SysRolePermissionEntity.class).as("rp").on("rp.permission_id = p.id")
                .join(RoleEntity.class).as("r").on("r.id = rp.role_id")
                .join(SysUserRoleEntity.class).as("ur").on("ur.role_id = r.id")
                .where("ur.user_id = ?", id)
                .and("(p.is_deleted = 0 OR p.is_deleted IS NULL)");
        List<SysPermissionEntity> rows = permissionMapper.selectListByQuery(qw);
        return rows.stream().map(e -> {
            PermissionResponse r = new PermissionResponse();
            BeanMapper.mapper(e, r);
            return r;
        }).collect(Collectors.toList());
    }
}
