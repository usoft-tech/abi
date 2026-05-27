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

import com.usoft.framework.system.api.DepartmentCreateRequest;
import com.usoft.framework.system.api.DepartmentQueryRequest;
import com.usoft.framework.system.api.DepartmentResponse;
import com.usoft.framework.system.api.DepartmentUpdateRequest;
import com.usoft.framework.system.service.DepartmentService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

/**
 * 部门控制器
 */
@RestController
@RequestMapping(value = "/api/system/departments", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "部门管理", authorize = "sa:dept")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 构造控制器
     */
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 创建部门
     */
    @PostMapping
    @AuthorizeDescription("创建部门")
    @PreAuthorize("@ss.hasAuthority('sa:dept:create')")
    public ApiResponse<DepartmentResponse> create(@Validated @RequestBody DepartmentCreateRequest req) {
        return ApiResponse.ok(departmentService.create(req));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新部门")
    @PreAuthorize("@ss.hasAuthority('sa:dept:update')")
    public ApiResponse<DepartmentResponse> update(@PathVariable String id, @Validated @RequestBody DepartmentUpdateRequest req) {
        return ApiResponse.ok(departmentService.update(id, req));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除部门")
    @PreAuthorize("@ss.hasAuthority('sa:dept:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(departmentService.delete(id));
    }

    /**
     * 查询部门详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("获取部门详情")
    @PreAuthorize("@ss.hasAuthority('sa:dept:get')")
    public ApiResponse<DepartmentResponse> get(@PathVariable String id) {
        return ApiResponse.ok(departmentService.get(id));
    }

    /**
     * 列出部门
     */
    @GetMapping
    @AuthorizeDescription("获取部门列表")
    @PreAuthorize("@ss.hasAuthority('sa:dept:list')")
    public ApiResponse<PageResponse<DepartmentResponse>> list(@Validated DepartmentQueryRequest req) {
        return ApiResponse.ok(departmentService.list(req));
    }
}
