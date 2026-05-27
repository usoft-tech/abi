package com.usoft.framework.bi.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.HttpStatus;
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

import com.usoft.framework.bi.api.AppCreateRequest;
import com.usoft.framework.bi.api.AppResponse;
import com.usoft.framework.bi.api.AppUpdateRequest;
import com.usoft.framework.bi.api.PageResponse;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.PageStatus;
import com.usoft.framework.bi.service.AppService;
import com.usoft.framework.bi.service.AuthorizationService;
import com.usoft.framework.bi.service.PageService;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.exception.HttpStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bi/apps", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "应用管理", authorize = "tenant:sa:app", group = true),
})
public class AppController {

    private final TenantService tenantService;
    private final AppService biAppService;
    private final AuthorizationService authorizationService;
    private final PageService pageService;

    /**
     * 创建应用
     * 
     * @param req 创建请求
     * @return 创建后的实体
     */
    @PostMapping
    @AuthorizeDescription(value = "创建应用", authorize = "tenant:sa:app:create")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:create')")
    public ApiResponse<AppResponse> create(@Validated @RequestBody AppCreateRequest req) {
        return ApiResponse.ok(biAppService.create(req));
    }

    /**
     * 更新应用
     * 
     * @param id  实体ID
     * @param req 更新请求
     * @return 更新后的实体
     */
    @PutMapping("/{id}")
    @AuthorizeDescription(value = "更新应用", authorize = "tenant:sa:app:update")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:update')")
    public ApiResponse<AppResponse> update(@PathVariable String id, @Validated @RequestBody AppUpdateRequest req) {
        return ApiResponse.ok(biAppService.update(id, req));
    }

    /**
     * 删除应用
     * 
     * @param id 实体ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription(value = "删除应用", authorize = "tenant:sa:app:delete")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(biAppService.delete(id));
    }

    /**
     * 查询应用详情
     * 
     * @param id 实体ID
     * @return 实体详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription(value = "查询应用详情", authorize = "tenant:sa:app:get")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:get')")
    public ApiResponse<AppResponse> get(@PathVariable String id) {
        return ApiResponse.ok(biAppService.get(id));
    }

    /**
     * 查询应用列表
     * 
     * @return 实体列表
     */
    @GetMapping
    @AuthorizeDescription(value = "查询应用列表", authorize = "tenant:sa:app:list")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:list')")
    public ApiResponse<List<AppResponse>> list() {
        return ApiResponse.ok(biAppService.list());
    }

    /**
     * 查询应用配置的页面详情
     * 
     * @param id 应用ID
     * @return 应用配置的页面详情
     */
    @GetMapping("/{id}/schema")
    @AuthorizeDescription(value = "查询应用配置的页面详情", authorize = "tenant:sa:app:schema")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:app:schema')")
    public ApiResponse<PageResponse> schema(@PathVariable String id) {
        boolean isAdmin = tenantService.isTenantUserAuthKey(TenantContext.getTenantId(), UserHolder.userId(),
                TenantAuthKey.ADMIN);
        if (!isAdmin) {
            authorizationService.checkAuth(TenantContext.getTenantId(), BizType.APP, id,
                    UserHolder.userId());
        }
        AppResponse app = biAppService.get(id);
        if (app == null || app.getHandle() == null || StringUtils.isBlank(app.getHandle().getPageId())) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND.value(), "应用未配置页面");
        }

        // Check tenant auth
        tenantService.checkTenantUserAuthKey(app.getTenantId(), UserHolder.userId(), TenantAuthKey.USER);

        PageResponse page = pageService.get(app.getHandle().getPageId());
        if (page == null || page.getStatus() != PageStatus.PUBLISHED) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND.value(), "应用配置的页面未发布");
        }
        page.setHasWatermark(app.getHandle().getHasWatermark());
        return ApiResponse.ok(page);
    }
}
