package com.usoft.framework.system.controller;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.system.api.DictDataCreateRequest;
import com.usoft.framework.system.api.DictDataQueryRequest;
import com.usoft.framework.system.api.DictDataResponse;
import com.usoft.framework.system.api.DictDataUpdateRequest;
import com.usoft.framework.system.service.DictDataService;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典数据管理
 */
@RestController
@RequestMapping(value = "/api/system/dict/data", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "字典数据管理", authorize = "sa:dict:data", group = true),
})
public class DictDataController {
    private final DictDataService dictDataService;

    public DictDataController(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    /**
     * 创建字典数据
     */
    @PostMapping
    @AuthorizeDescription("创建字典数据")
    @PreAuthorize("@ss.hasAuthority('sa:dict:data:create')")
    public ApiResponse<DictDataResponse> create(@Validated @RequestBody DictDataCreateRequest req) {
        return ApiResponse.ok(dictDataService.create(req));
    }

    /**
     * 更新字典数据
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新字典数据")
    @PreAuthorize("@ss.hasAuthority('sa:dict:data:update')")
    public ApiResponse<DictDataResponse> update(@PathVariable String id, @Validated @RequestBody DictDataUpdateRequest req) {
        return ApiResponse.ok(dictDataService.update(id, req));
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除字典数据")
    @PreAuthorize("@ss.hasAuthority('sa:dict:data:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(dictDataService.delete(id));
    }

    /**
     * 获取字典数据详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("获取字典数据详情")
    @PreAuthorize("@ss.hasAuthority('sa:dict:data:get')")
    public ApiResponse<DictDataResponse> get(@PathVariable String id) {
        return ApiResponse.ok(dictDataService.get(id));
    }

    /**
     * 分页查询字典数据
     */
    @GetMapping
    @AuthorizeDescription("分页查询字典数据")
    @PreAuthorize("@ss.hasAuthority('sa:dict:data:list')")
    public ApiResponse<PageResponse<DictDataResponse>> list(@Validated DictDataQueryRequest req) {
        return ApiResponse.ok(dictDataService.list(req));
    }

    /**
     * 根据字典类型获取字典数据列表
     */
    @GetMapping("/type/{dictType}")
    @AuthorizeDescription("根据字典类型获取字典数据列表")
    public ApiResponse<java.util.List<DictDataResponse>> listByType(@PathVariable String dictType) {
        return ApiResponse.ok(dictDataService.listByType(dictType));
    }
}
