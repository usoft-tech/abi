package com.usoft.framework.bi.controller;

import java.util.List;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.bi.api.BatchUpdateAuthRequest;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.entity.AuthorizationEntity;
import com.usoft.framework.bi.service.AuthorizationService;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.common.api.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bi/authorization", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "授权管理", authorize = "tenant:sa:auth", group = true)
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;
    private final TenantService tenantService;

    /**
     * 批量更新授权（全量替换）
     */
    @PostMapping("/batch-update")
    @AuthorizeDescription(value = "批量更新授权", authorize = "tenant:sa:auth:save")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:auth:save')")
    public ApiResponse<Boolean> batchUpdate(@Validated @RequestBody BatchUpdateAuthRequest req) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        authorizationService.update(req);
        return ApiResponse.ok(true);
    }

    /**
     * 根据业务ID查询授权列表
     */
    @GetMapping("/list")
    @AuthorizeDescription(value = "根据业务ID查询授权列表", authorize = "tenant:sa:auth:list")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:auth:list')")
    public ApiResponse<List<AuthorizationEntity>> list(@RequestParam BizType bizType, @RequestParam String bizId) {
        tenantService.checkTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(authorizationService.listByBizId(bizType, bizId));
    }

}
