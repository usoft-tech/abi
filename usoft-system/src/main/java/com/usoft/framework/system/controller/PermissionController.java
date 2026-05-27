package com.usoft.framework.system.controller;

import java.util.List;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.system.api.PermissionCreateRequest;
import com.usoft.framework.system.api.PermissionQueryRequest;
import com.usoft.framework.system.api.PermissionResponse;
import com.usoft.framework.system.api.PermissionUpdateRequest;
import com.usoft.framework.system.service.PermissionService;
import com.usoft.framework.common.api.ApiResponse;

/**
 * 权限管理
 */
@RestController
@RequestMapping(value = "/api/system/permission", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "权限管理", authorize = "sa:permission", group = true)
})
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 权限列表
     */
    @GetMapping("/list")
    @AuthorizeDescription("权限列表")
    @PreAuthorize("@ss.hasAuthority('sa:permission:list')")
    public ApiResponse<List<PermissionResponse>> list(PermissionQueryRequest req) {
        return ApiResponse.ok(permissionService.list(req));
    }

    /**
     * 创建权限
     */
    @PostMapping("/create")
    @AuthorizeDescription("创建权限")
    @PreAuthorize("@ss.hasAuthority('sa:permission:create')")
    public ApiResponse<PermissionResponse> create(@RequestBody PermissionCreateRequest req) {
        return ApiResponse.ok(permissionService.create(req));
    }

    /**
     * 更新权限
     */
    @PostMapping("/update/{id}")
    @AuthorizeDescription("更新权限")
    @PreAuthorize("@ss.hasAuthority('sa:permission:update')")
    public ApiResponse<PermissionResponse> update(@PathVariable String id, @RequestBody PermissionUpdateRequest req) {
        return ApiResponse.ok(permissionService.update(id, req));
    }

    /**
     * 删除权限
     */
    @PostMapping("/delete/{id}")
    @AuthorizeDescription("删除权限")
    @PreAuthorize("@ss.hasAuthority('sa:permission:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        permissionService.delete(id);
        return ApiResponse.ok(true);
    }
}
