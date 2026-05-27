package com.usoft.framework.system.controller;

import java.util.List;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

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
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.system.api.RoleCreateRequest;
import com.usoft.framework.system.api.RoleQueryRequest;
import com.usoft.framework.system.api.RoleResponse;
import com.usoft.framework.system.api.RoleUpdateRequest;
import com.usoft.framework.system.api.UserResponse;
import com.usoft.framework.system.service.RoleService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.api.PageResponse;

/**
 * 角色控制器
 */
@RestController
@RequestMapping(value = "/api/system/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "角色管理", authorize = "sa:role", group = true)
})
public class RoleController {

    private final RoleService roleService;

    /**
     * 构造控制器
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 创建角色
     */
    @PostMapping
    @AuthorizeDescription("创建角色")
    @PreAuthorize("@ss.hasAuthority('sa:role:create')")
    public ApiResponse<RoleResponse> create(@Validated @RequestBody RoleCreateRequest req) {
        return ApiResponse.ok(roleService.create(req));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新角色")
    @PreAuthorize("@ss.hasAuthority('sa:role:update')")
    public ApiResponse<RoleResponse> update(@PathVariable String id, @Validated @RequestBody RoleUpdateRequest req) {
        return ApiResponse.ok(roleService.update(id, req));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除角色")
    @PreAuthorize("@ss.hasAuthority('sa:role:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(roleService.delete(id));
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("查询角色详情")
    @PreAuthorize("@ss.hasAuthority('sa:role:get')")
    public ApiResponse<RoleResponse> get(@PathVariable String id) {
        return ApiResponse.ok(roleService.get(id));
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/{id}/permissions")
    @AuthorizeDescription("获取角色权限")
    @PreAuthorize("@ss.hasAuthority('sa:role:get')")
    public ApiResponse<List<String>> listPermissions(@PathVariable String id) {
        return ApiResponse.ok(roleService.listPermissions(id));
    }

    /**
     * 分配角色权限
     */
    @PostMapping("/{id}/permissions")
    @AuthorizeDescription("分配角色权限")
    @PreAuthorize("@ss.hasAuthority('sa:role:update')")
    public ApiResponse<Boolean> assignPermissions(@PathVariable String id, @RequestBody List<String> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ApiResponse.ok(true);
    }

    /**
     * 获取角色下的用户列表
     */
    @GetMapping("/{id}/users")
    @AuthorizeDescription("获取角色下的用户列表")
    @PreAuthorize("@ss.hasAuthority('sa:role:get')")
    public ApiResponse<PageResponse<UserResponse>> listRoleUsers(@PathVariable String id, PageRequest req) {
        return ApiResponse.ok(roleService.listRoleUsers(id, req));
    }

    /**
     * 给角色添加用户
     */
    @PostMapping("/{id}/users")
    @AuthorizeDescription("给角色添加用户")
    @PreAuthorize("@ss.hasAuthority('sa:role:update')")
    public ApiResponse<Boolean> addRoleUsers(@PathVariable String id, @RequestBody List<String> userIds) {
        roleService.addRoleUsers(id, userIds);
        return ApiResponse.ok(true);
    }

    /**
     * 从角色移除用户
     */
    @DeleteMapping("/{id}/users/{userId}")
    @AuthorizeDescription("从角色移除用户")
    @PreAuthorize("@ss.hasAuthority('sa:role:update')")
    public ApiResponse<Boolean> removeRoleUser(@PathVariable String id, @PathVariable String userId) {
        roleService.removeRoleUser(id, userId);
        return ApiResponse.ok(true);
    }

    /**
     * 列出角色
     */
    @GetMapping
    @AuthorizeDescription("分页查询角色")
    @PreAuthorize("@ss.hasAuthority('sa:role:list')")
    public ApiResponse<PageResponse<RoleResponse>> list(@Validated RoleQueryRequest req) {
        return ApiResponse.ok(roleService.list(req));
    }
}
