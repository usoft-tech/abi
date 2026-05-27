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

import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.TenantCreateRequest;
import com.usoft.framework.system.api.TenantQueryRequest;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.api.TenantSettingRequest;
import com.usoft.framework.system.api.TenantUpdateRequest;
import com.usoft.framework.system.api.TenantUserResponse;
import com.usoft.framework.system.api.UserQueryRequest;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

@RestController
@RequestMapping(value = "/api/system/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "租户", authorize = "tenant", group = true),
    @AuthorizeDescription(value = "租户管理", authorize = "tenant:sa", group = true),
    @AuthorizeDescription(value = "租户管理", authorize = "sa:tenant", group = true),
    @AuthorizeDescription(value = "租户用户管理", authorize = "sa:tenant:users", group = true),
})
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @AuthorizeDescription("创建租户")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:create')")
    public ApiResponse<TenantResponse> create(@Validated @RequestBody TenantCreateRequest req) {
        return ApiResponse.ok(tenantService.create(req));
    }

    @PutMapping("/{id}")
    @AuthorizeDescription("更新租户")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:update')")
    public ApiResponse<TenantResponse> update(@PathVariable String id,
            @Validated @RequestBody TenantUpdateRequest req) {
        tenantService.checkTenantUserAuthKey(id, UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(tenantService.update(id, req));
    }

    @GetMapping("/setting")
    @AuthorizeDescription("获取租户设置")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:setting')")
    public ApiResponse<TenantResponse> setting() {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(tenantService.get(TenantContext.getTenantId()));
    }

    @PutMapping("/setting")
    @AuthorizeDescription("更新租户设置")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:setting')")
    public ApiResponse<TenantResponse> setting(@Validated @RequestBody TenantSettingRequest req) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(tenantService.setting(TenantContext.getTenantId(), req));
    }

    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除租户")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(tenantService.delete(id));
    }

    @GetMapping("/{id}")
    @AuthorizeDescription("获取租户详情")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:get')")
    public ApiResponse<TenantResponse> get(@PathVariable String id) {
        return ApiResponse.ok(tenantService.get(id));
    }

    @GetMapping
    @AuthorizeDescription("获取租户列表")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:list')")
    public ApiResponse<PageResponse<TenantResponse>> list(@Validated TenantQueryRequest req) {
        return ApiResponse.ok(tenantService.list(req));
    }

    @GetMapping("/{id}/users")
    @AuthorizeDescription("获取租户用户列表")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:users')")
    public ApiResponse<PageResponse<TenantUserResponse>> listUsers(@PathVariable String id,
            @Validated UserQueryRequest req) {
        return ApiResponse.ok(tenantService.listTenantUsers(id, req));
    }

    @PostMapping("/{id}/users")
    @AuthorizeDescription("添加租户用户")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:users:add')")
    public ApiResponse<Boolean> addUsers(@PathVariable String id, @RequestBody List<String> userIds) {
        tenantService.addTenantUsers(id, userIds);
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/{id}/users/{userId}")
    @AuthorizeDescription("移除租户用户")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:users:remove')")
    public ApiResponse<Boolean> removeUser(@PathVariable String id, @PathVariable String userId) {
        tenantService.removeTenantUser(id, userId);
        return ApiResponse.ok(true);
    }

    @PutMapping("/{id}/auth/{userId}/{authKey}")
    @AuthorizeDescription("更新租户用户角色")
    @PreAuthorize("@ss.hasAuthority('sa:tenant:users:update')")
    public ApiResponse<Boolean> changeUserAuthKey(@PathVariable String id, @PathVariable String userId,
            @PathVariable TenantAuthKey authKey) {
        tenantService.changeTenantUserAuthKey(id, userId, authKey);
        return ApiResponse.ok(true);
    }

    @GetMapping("/users")
    @AuthorizeDescription("获取本地租户用户列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:users')")
    public ApiResponse<PageResponse<TenantUserResponse>> localListUsers(@Validated UserQueryRequest req) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(tenantService.listTenantUsers(TenantContext.getTenantId(), req));
    }

    @PostMapping("/users")
    @AuthorizeDescription("添加本地租户用户")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:users:add')")
    public ApiResponse<Boolean> addLocalUsers(@RequestBody List<String> userIds) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        tenantService.addTenantUsers(TenantContext.getTenantId(), userIds);
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/users/{userId}")
    @AuthorizeDescription("移除本地租户用户")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:users:remove')")
    public ApiResponse<Boolean> removeLocalUser(@PathVariable String userId) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        tenantService.removeTenantUser(TenantContext.getTenantId(), userId);
        return ApiResponse.ok(true);
    }

    @PutMapping("/auth/{userId}/{authKey}")
    @AuthorizeDescription("更新本地租户用户角色")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:users:update')")
    public ApiResponse<Boolean> changeLocalUserAuthKey(@PathVariable String userId,
            @PathVariable TenantAuthKey authKey) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        tenantService.changeTenantUserAuthKey(TenantContext.getTenantId(), userId, authKey);
        return ApiResponse.ok(true);
    }
}
