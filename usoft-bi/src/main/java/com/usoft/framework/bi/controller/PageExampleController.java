package com.usoft.framework.bi.controller;

import com.usoft.framework.bi.api.PageExampleCreateRequest;
import com.usoft.framework.bi.api.PageExampleResponse;
import com.usoft.framework.bi.api.PageExampleUpdateRequest;
import com.usoft.framework.bi.service.PageExampleService;
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
 * 页面片段控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/bi/page-examples", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "页面元素示例", authorize = "tenant:sa:page-example", group = true),
    @AuthorizeDescription(value = "页面元素示例", authorize = "sa:page-example", group = true),
})
public class PageExampleController {
    private final PageExampleService service;

    /**
     * 分页查询页面片段
     */
    @GetMapping
    @AuthorizeDescriptionGroup({
            @AuthorizeDescription(value = "查询页面元素示例列表", authorize = "sa:page-example:list"),
            @AuthorizeDescription(value = "查询页面元素示例列表", authorize = "tenant:sa:page-example:list"),
    })
    @PreAuthorize("@ss.hasAnyAuthority('sa:page-example:list', 'tenant:sa:page-example:list')")
    public ApiResponse<PageResponse<PageExampleResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.list(page, size, keyword));
    }

    /**
     * 创建页面片段
     */
    @PostMapping
    @AuthorizeDescription("创建页面元素示例")
    @PreAuthorize("@ss.hasAuthority('sa:page-example:create')")
    public ApiResponse<PageExampleResponse> create(@Validated @RequestBody PageExampleCreateRequest req) {
        return ApiResponse.ok(service.create(req));
    }

    /**
     * 更新页面片段
     */
    @PutMapping
    @AuthorizeDescription("更新页面元素示例")
    @PreAuthorize("@ss.hasAuthority('sa:page-example:update')")
    public ApiResponse<PageExampleResponse> update(@Validated @RequestBody PageExampleUpdateRequest req) {
        return ApiResponse.ok(service.update(req));
    }

    /**
     * 删除页面片段
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除页面元素示例")
    @PreAuthorize("@ss.hasAuthority('sa:page-example:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.ok(true);
    }

    /**
     * 获取页面片段详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescriptionGroup({
            @AuthorizeDescription(value = "获取页面元素示例", authorize = "sa:page-example:get"),
            @AuthorizeDescription(value = "获取页面元素示例", authorize = "tenant:sa:page-example:get"),
    })
    @PreAuthorize("@ss.hasAnyAuthority('sa:page-example:get', 'tenant:sa:page-example:get')")
    public ApiResponse<PageExampleResponse> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }
}
