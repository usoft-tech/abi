package com.usoft.framework.bi.controller;

import com.usoft.framework.bi.api.PageTemplateCreateRequest;
import com.usoft.framework.bi.api.PageTemplateResponse;
import com.usoft.framework.bi.api.PageTemplateUpdateRequest;
import com.usoft.framework.bi.service.PageTemplateService;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 页面模板控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bi/page-templates", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
        @AuthorizeDescription(value = "页面模板", authorize = "tenant:sa:page-template", group = true),
        @AuthorizeDescription(value = "页面模板", authorize = "sa:page-template", group = true),
})
public class PageTemplateController {
    private final PageTemplateService service;

    /**
     * 分页查询页面模板
     */
    @GetMapping
    @AuthorizeDescriptionGroup({
            @AuthorizeDescription(value = "查询页面模板列表", authorize = "sa:page-template:list"),
            @AuthorizeDescription(value = "查询页面模板列表", authorize = "tenant:sa:page-template:list"),
    })
    @AuthorizeDescription("查询页面模板列表")
    @PreAuthorize("@ss.hasAnyAuthority('sa:page-template:list', 'tenant:sa:page-template:list')")
    public ApiResponse<PageResponse<PageTemplateResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.list(page, size, keyword));
    }

    /**
     * 创建页面模板
     */
    @PostMapping
    @AuthorizeDescription("创建页面模板")
    @PreAuthorize("@ss.hasAuthority('sa:page-template:create')")
    public ApiResponse<PageTemplateResponse> create(@Validated @RequestBody PageTemplateCreateRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    /**
     * 更新页面模板
     */
    @PutMapping
    @AuthorizeDescription("更新页面模板")
    @PreAuthorize("@ss.hasAuthority('sa:page-template:update')")
    public ApiResponse<PageTemplateResponse> update(@Validated @RequestBody PageTemplateUpdateRequest req) {
        return ApiResponse.ok(service.update(req));
    }

    /**
     * 删除页面模板
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除页面模板")
    @PreAuthorize("@ss.hasAuthority('sa:page-template:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.ok(true);
    }

    /**
     * 获取页面模板详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescriptionGroup({
            @AuthorizeDescription(value = "获取页面模板", authorize = "sa:page-template:get"),
            @AuthorizeDescription(value = "获取页面模板", authorize = "tenant:sa:page-template:get"),
    })
    @PreAuthorize("@ss.hasAnyAuthority('sa:page-template:get', 'tenant:sa:page-template:get')")
    public ApiResponse<PageTemplateResponse> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }
}
