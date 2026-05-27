package com.usoft.framework.system.controller;

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

import com.usoft.framework.system.api.TenantUserResponse;
import com.usoft.framework.system.api.UserCreateRequest;
import com.usoft.framework.system.api.UserQueryRequest;
import com.usoft.framework.system.api.UserResponse;
import com.usoft.framework.system.api.UserUpdateRequest;
import com.usoft.framework.system.service.UserService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

/**
 * 用户控制器
 */
@RestController
@RequestMapping(value = "/api/system/users", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "全局数据", authorize = "global", group = true),
    @AuthorizeDescription(value = "用户管理", authorize = "sa:user", group = true),
    @AuthorizeDescription(value = "用户", authorize = "global:user", group = true),
})
public class UserController {

    private final UserService userService;

    /**
     * 构造控制器
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    @AuthorizeDescription("更新个人信息")
    public ApiResponse<UserResponse> updateProfile(@RequestBody com.usoft.framework.system.api.UserProfileUpdateRequest req) {
        String userId = com.usoft.framework.core.security.UserHolder.userId();
        return ApiResponse.ok(userService.updateProfile(userId, req));
    }

    /**
     * 创建用户接口
     */
    @PostMapping
    @AuthorizeDescription("创建用户")
    @PreAuthorize("@ss.hasAuthority('sa:user:create')")
    public ApiResponse<UserResponse> create(@Validated @RequestBody UserCreateRequest req) {
        return ApiResponse.ok(userService.create(req));
    }

    /**
     * 更新用户接口
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新用户")
    @PreAuthorize("@ss.hasAuthority('sa:user:update')")
    public ApiResponse<UserResponse> update(@PathVariable String id, @Validated @RequestBody UserUpdateRequest req) {
        if ("1".equals(id)) {
            throw new IllegalArgumentException("不能更新默认用户");
        }
        return ApiResponse.ok(userService.update(id, req));
    }

    /**
     * 删除用户接口
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除用户")
    @PreAuthorize("@ss.hasAuthority('sa:user:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        if ("1".equals(id)) {
            throw new IllegalArgumentException("不能删除默认用户");
        }
        return ApiResponse.ok(userService.delete(id));
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("查询用户详情")
    @PreAuthorize("@ss.hasAuthority('sa:user:get')")
    public ApiResponse<UserResponse> get(@PathVariable String id) {
        return ApiResponse.ok(userService.get(id));
    }

    /**
     * 列出用户
     */
    @GetMapping
    @AuthorizeDescription("查询租户用户列表")
    @PreAuthorize("@ss.hasAuthority('sa:user:list')")
    public ApiResponse<PageResponse<TenantUserResponse>> listFromTenant(@Validated UserQueryRequest req) {
        return ApiResponse.ok(userService.listFromTenant(req));
    }

    /**
     * 列出用户
     */
    @GetMapping("/global")
    @AuthorizeDescription("查询全局用户列表")
    @PreAuthorize("@ss.hasAuthority('global:user:list')")
    public ApiResponse<PageResponse<UserResponse>> listFromGlobal(@Validated UserQueryRequest req) {
        return ApiResponse.ok(userService.list(req));
    }
}
