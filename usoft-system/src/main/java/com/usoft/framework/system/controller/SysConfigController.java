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

import com.usoft.framework.system.api.SysConfigCreateRequest;
import com.usoft.framework.system.api.SysConfigQueryRequest;
import com.usoft.framework.system.api.SysConfigResponse;
import com.usoft.framework.system.api.SysConfigUpdateRequest;
import com.usoft.framework.system.service.SysConfigService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping(value = "/api/system/configs", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "系统管理", authorize = "sa", group = true),
    @AuthorizeDescription(value = "系统配置管理", authorize = "sa:setting", group = true),
    @AuthorizeDescription(value = "系统配置", authorize = "global:setting", group = true),
})
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 构造控制器
     */
    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    /**
     * 创建系统配置
     */
    @PostMapping
    @AuthorizeDescription("创建系统配置")
    @PreAuthorize("@ss.hasAuthority('sa:setting:create')")
    public ApiResponse<SysConfigResponse> create(@Validated @RequestBody SysConfigCreateRequest req) {
        return ApiResponse.ok(sysConfigService.create(req));
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新系统配置")
    @PreAuthorize("@ss.hasAuthority('sa:setting:update')")
    public ApiResponse<SysConfigResponse> update(@PathVariable String id, @Validated @RequestBody SysConfigUpdateRequest req) {
        return ApiResponse.ok(sysConfigService.update(id, req));
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除系统配置")
    @PreAuthorize("@ss.hasAuthority('sa:setting:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(sysConfigService.delete(id));
    }

    /**
     * 查询系统配置详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("查询系统配置详情")
    @PreAuthorize("@ss.hasAuthority('sa:setting:get')")
    public ApiResponse<SysConfigResponse> get(@PathVariable String id) {
        return ApiResponse.ok(sysConfigService.get(id));
    }

    /**
     * 列出系统配置
     */
    @GetMapping
    @AuthorizeDescriptionGroup({
        @AuthorizeDescription(value = "查询系统配置", authorize = "sa:setting:list"),
        @AuthorizeDescription(value = "查询系统配置", authorize = "global:setting:list"),
    })
    @PreAuthorize("@ss.hasAnyAuthority('sa:setting:list', 'global:setting:list')")
    public ApiResponse<PageResponse<SysConfigResponse>> list(@Validated SysConfigQueryRequest req) {
        return ApiResponse.ok(sysConfigService.list(req));
    }
}
