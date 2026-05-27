package com.usoft.framework.bi.controller;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.bi.api.PageReportQueryRequest;
import com.usoft.framework.bi.api.PageReportResponse;
import com.usoft.framework.bi.api.PageReportUpdateRequest;
import com.usoft.framework.bi.service.PageReportService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 页面报告管理
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/bi/page-reports", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "页面报告管理", authorize = "tenant:sa:report")
public class PageReportController {

    private final PageReportService reportService;

    /**
     * 更新页面报告
     *
     * @param id ID
     * @param req 请求
     * @return 响应
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新页面报告")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:report:update')")
    public ApiResponse<PageReportResponse> update(@PathVariable String id,
            @Validated @RequestBody PageReportUpdateRequest req) {
        return ApiResponse.ok(reportService.update(id, req));
    }

    /**
     * 获取页面报告详情
     *
     * @param id ID
     * @return 响应
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("获取页面报告详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:report:get')")
    public ApiResponse<PageReportResponse> get(@PathVariable String id) {
        return ApiResponse.ok(reportService.get(id));
    }

    /**
     * 根据页面ID查询报告列表
     *
     * @param req 查询请求
     * @return 响应
     */
    @GetMapping("/page/{pageId}")
    @AuthorizeDescription("根据页面ID查询报告列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:report:list')")
    public ApiResponse<PageResponse<PageReportResponse>> listByPageId(@PathVariable String pageId, PageReportQueryRequest req) {
        req.setPageId(pageId);
        return ApiResponse.ok(reportService.listByPageId(req));
    }

    /**
     * 删除页面报告
     *
     * @param id ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除页面报告")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:report:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        reportService.delete(id);
        return ApiResponse.ok(true);
    }
}
