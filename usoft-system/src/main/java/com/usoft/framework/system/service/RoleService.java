package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.system.api.RoleCreateRequest;
import com.usoft.framework.system.api.RoleQueryRequest;
import com.usoft.framework.system.api.RoleResponse;
import com.usoft.framework.system.api.RoleUpdateRequest;
import com.usoft.framework.system.api.UserResponse;
import com.usoft.framework.system.entity.RoleEntity;
import com.usoft.framework.system.entity.SysRolePermissionEntity;
import com.usoft.framework.system.entity.SysUserRoleEntity;
import com.usoft.framework.system.mapper.RoleMapper;
import com.usoft.framework.system.mapper.SysRolePermissionMapper;
import com.usoft.framework.system.mapper.SysUserRoleMapper;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.common.api.PageResponse;

/**
 * 角色服务
 */
@Service
public class RoleService {
    private final RoleMapper roleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final UserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 构造服务
     */
    public RoleService(RoleMapper roleMapper, SysRolePermissionMapper sysRolePermissionMapper,
            UserMapper sysUserMapper, SysUserRoleMapper sysUserRoleMapper) {
        this.roleMapper = roleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    /**
     * 创建角色
     */
    public RoleResponse create(RoleCreateRequest req) {
        RoleEntity e = new RoleEntity();
        e.setId(UUID.randomUUID().toString());
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        roleMapper.insert(e);
        RoleResponse r = new RoleResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setCode(e.getCode());
        return r;
    }

    /**
     * 更新角色
     */
    public RoleResponse update(String id, RoleUpdateRequest req) {
        RoleEntity e = roleMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        roleMapper.update(e);
        RoleResponse r = new RoleResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setCode(e.getCode());
        return r;
    }

    /**
     * 删除角色
     */
    public boolean delete(String id) {
        RoleEntity e = roleMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return roleMapper.update(e) > 0;
    }

    /**
     * 查询角色详情
     */
    public RoleResponse get(String id) {
        RoleEntity e = roleMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        RoleResponse r = new RoleResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        return r;
    }

    /**
     * 列出当前租户的角色（分页筛选）
     */
    public PageResponse<RoleResponse> list(RoleQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getCode() != null && !req.getCode().isBlank()) {
            qw.and("code LIKE ?", "%" + req.getCode() + "%");
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ? OR code LIKE ?)", kw, kw);
        }
        return PageHelper.apply(roleMapper, qw, req, e -> {
            RoleResponse r = new RoleResponse();
            r.setId(e.getId());
            r.setName(e.getName());
            r.setCode(e.getCode());
            return r;
        });
    }

    /**
     * 获取角色权限ID列表
     */
    public List<String> listPermissions(String roleId) {
        QueryWrapper qw = QueryWrapper.create()
                .select("permission_id")
                .where("role_id = ?", roleId);
        return sysRolePermissionMapper.selectObjectListByQueryAs(qw, String.class);
    }

    /**
     * 分配角色权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleId, List<String> permissionIds) {
        // 删除旧权限
        QueryWrapper qw = QueryWrapper.create()
                .where("role_id = ?", roleId);
        sysRolePermissionMapper.deleteByQuery(qw);

        // 插入新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermissionEntity> list = new ArrayList<>();
            for (String pid : permissionIds) {
                SysRolePermissionEntity e = new SysRolePermissionEntity();
                e.setRoleId(roleId);
                e.setPermissionId(pid);
                list.add(e);
            }
            sysRolePermissionMapper.insertBatch(list);
        }
    }

    /**
     * 列出角色下的用户
     */
    public PageResponse<UserResponse> listRoleUsers(String roleId, com.usoft.framework.common.api.PageRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .select("u.*")
                .from("sys_user").as("u")
                .join("sys_user_role").as("ur").on("u.id = ur.user_id")
                .where("ur.role_id = ?", roleId)
                .and("u.is_deleted = 0");
        
        return PageHelper.apply(sysUserMapper, qw, req, e -> {
             UserResponse r = new UserResponse();
             r.setId(e.getId());
             r.setUsername(e.getUsername());
             r.setDisplayName(e.getDisplayName());
             r.setEmployeeNo(e.getEmployeeNo());
             r.setStatus(e.getStatus());
             return r;
        });
    }

    /**
     * 给角色添加用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRoleUsers(String roleId, List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<SysUserRoleEntity> list = new ArrayList<>();
        for (String uid : userIds) {
            QueryWrapper qw = QueryWrapper.create()
                    .where("role_id = ?", roleId)
                    .and("user_id = ?", uid);
            if (sysUserRoleMapper.selectCountByQuery(qw) == 0) {
                 SysUserRoleEntity e = new SysUserRoleEntity();
                 e.setRoleId(roleId);
                 e.setUserId(uid);
                 list.add(e);
            }
        }
        if (!list.isEmpty()) {
            sysUserRoleMapper.insertBatch(list);
        }
    }

    /**
     * 从角色移除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleUser(String roleId, String userId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("role_id = ?", roleId)
                .and("user_id = ?", userId);
        sysUserRoleMapper.deleteByQuery(qw);
    }

}
