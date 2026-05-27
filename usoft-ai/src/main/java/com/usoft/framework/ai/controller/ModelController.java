package com.usoft.framework.ai.controller;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.ai.service.ModelService;
import com.usoft.framework.ai.api.ModelCreateRequest;
import com.usoft.framework.ai.api.ModelQueryRequest;
import com.usoft.framework.ai.api.ModelResponse;
import com.usoft.framework.ai.api.ModelUpdateRequest;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

@RestController
@RequestMapping(value = "/api/ai/models", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "大模型管理", authorize = "ai:model", group = true)
})
public class ModelController {
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    @AuthorizeDescription(value = "创建模型", authorize = "ai:model:create")
    @PreAuthorize("@ss.hasAuthority('ai:model:create')")
    public ApiResponse<ModelResponse> create(@Validated @RequestBody ModelCreateRequest req) {
        return ApiResponse.ok(modelService.create(req));
    }

    @PutMapping("/{id}")
    @AuthorizeDescription(value = "更新模型", authorize = "ai:model:update")
    @PreAuthorize("@ss.hasAuthority('ai:model:update')")
    public ApiResponse<ModelResponse> update(@PathVariable String id, @Validated @RequestBody ModelUpdateRequest req) {
        return ApiResponse.ok(modelService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @AuthorizeDescription(value = "删除模型", authorize = "ai:model:delete")
    @PreAuthorize("@ss.hasAuthority('ai:model:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(modelService.delete(id));
    }

    @GetMapping("/{id}")
    @AuthorizeDescription(value = "获取模型详情", authorize = "ai:model:get")
    @PreAuthorize("@ss.hasAuthority('ai:model:get')")
    public ApiResponse<ModelResponse> get(@PathVariable String id) {
        return ApiResponse.ok(modelService.get(id));
    }

    @GetMapping
    @AuthorizeDescription(value = "分页查询模型", authorize = "ai:model:list")
    @PreAuthorize("@ss.hasAuthority('ai:model:list')")
    public ApiResponse<PageResponse<ModelResponse>> list(@Validated ModelQueryRequest req) {
        return ApiResponse.ok(modelService.list(req));
    }
}
