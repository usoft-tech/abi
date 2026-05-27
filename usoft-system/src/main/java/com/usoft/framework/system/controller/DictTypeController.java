package com.usoft.framework.system.controller;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.system.api.DictTypeCreateRequest;
import com.usoft.framework.system.api.DictTypeQueryRequest;
import com.usoft.framework.system.api.DictTypeResponse;
import com.usoft.framework.system.api.DictTypeUpdateRequest;
import com.usoft.framework.system.service.DictTypeService;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典类型管理
 */
@RestController
@RequestMapping(value = "/api/system/dict/type", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "字典管理", authorize = "sa:dict"),
    @AuthorizeDescription(value = "字典类型管理", authorize = "sa:dict:type"),
})
public class DictTypeController {
    private final DictTypeService dictTypeService;

    public DictTypeController(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    /**
     * 创建字典类型
     */
    @PostMapping
    @AuthorizeDescription("创建字典类型")
    @PreAuthorize("@ss.hasAuthority('sa:dict:type:create')")
    public ApiResponse<DictTypeResponse> create(@Validated @RequestBody DictTypeCreateRequest req) {
        return ApiResponse.ok(dictTypeService.create(req));
    }

    /**
     * 更新字典类型
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新字典类型")
    @PreAuthorize("@ss.hasAuthority('sa:dict:type:update')")
    public ApiResponse<DictTypeResponse> update(@PathVariable String id, @Validated @RequestBody DictTypeUpdateRequest req) {
        return ApiResponse.ok(dictTypeService.update(id, req));
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除字典类型")
    @PreAuthorize("@ss.hasAuthority('sa:dict:type:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(dictTypeService.delete(id));
    }

    /**
     * 获取字典类型详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("获取字典类型详情")
    @PreAuthorize("@ss.hasAuthority('sa:dict:type:get')")
    public ApiResponse<DictTypeResponse> get(@PathVariable String id) {
        return ApiResponse.ok(dictTypeService.get(id));
    }

    /**
     * 分页查询字典类型
     */
    @GetMapping
    @AuthorizeDescription("分页查询字典类型")
    @PreAuthorize("@ss.hasAuthority('sa:dict:type:list')")
    public ApiResponse<PageResponse<DictTypeResponse>> list(@Validated DictTypeQueryRequest req) {
        return ApiResponse.ok(dictTypeService.list(req));
    }
}
