package com.usoft.framework.system.controller;

import com.usoft.framework.common.annotation.AuthorizeDescription;
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

import com.usoft.framework.system.api.SysAuthClientCreateRequest;
import com.usoft.framework.system.api.SysAuthClientQueryRequest;
import com.usoft.framework.system.api.SysAuthClientResponse;
import com.usoft.framework.system.api.SysAuthClientUpdateRequest;
import com.usoft.framework.system.service.SysAuthClientService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

/**
 * 客户端凭证控制器
 */
@RestController
@RequestMapping(value = "/api/system/auth-clients", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "客户端凭证管理", authorize = "tenant:sa:sk", group = true)
public class SysAuthClientController {

    private final SysAuthClientService sysAuthClientService;

    /**
     * 构造控制器
     */
    public SysAuthClientController(SysAuthClientService sysAuthClientService) {
        this.sysAuthClientService = sysAuthClientService;
    }

    /**
     * 创建客户端凭证
     */
    @PostMapping
    @AuthorizeDescription("创建客户端凭证")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:create')")
    public ApiResponse<SysAuthClientResponse> create(@Validated @RequestBody SysAuthClientCreateRequest req) {
        return ApiResponse.ok(sysAuthClientService.create(req));
    }

    /**
     * 更新客户端凭证
     */
    @PutMapping("/{clientId}")
    @AuthorizeDescription("更新客户端凭证")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:update')")
    public ApiResponse<SysAuthClientResponse> update(@PathVariable String clientId,
            @Validated @RequestBody SysAuthClientUpdateRequest req) {
        return ApiResponse.ok(sysAuthClientService.update(clientId, req));
    }

    /**
     * 删除客户端凭证
     */
    @DeleteMapping("/{clientId}")
    @AuthorizeDescription("删除客户端凭证")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String clientId) {
        return ApiResponse.ok(sysAuthClientService.delete(clientId));
    }

    /**
     * 查询客户端凭证详情
     */
    @GetMapping("/{clientId}")
    @AuthorizeDescription("查询客户端凭证详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:get')")
    public ApiResponse<SysAuthClientResponse> get(@PathVariable String clientId) {
        return ApiResponse.ok(sysAuthClientService.get(clientId));
    }

    /**
     * 分页查询客户端凭证
     */
    @GetMapping
    @AuthorizeDescription("分页查询客户端凭证")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:list')")
    public ApiResponse<PageResponse<SysAuthClientResponse>> list(@Validated SysAuthClientQueryRequest req) {
        return ApiResponse.ok(sysAuthClientService.list(req));
    }

    /**
     * 刷新客户端密钥
     */
    @PostMapping("/{clientId}/refresh-secret")
    @AuthorizeDescription("刷新客户端密钥")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:sk:refresh-secret')")
    public ApiResponse<SysAuthClientResponse> refreshSecret(@PathVariable String clientId) {
        return ApiResponse.ok(sysAuthClientService.refreshSecret(clientId));
    }
}
