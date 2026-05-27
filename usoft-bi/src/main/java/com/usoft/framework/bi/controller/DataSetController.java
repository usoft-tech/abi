package com.usoft.framework.bi.controller;

import java.util.List;
import java.util.Map;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.bi.api.DataSetAssistantChatRequest;
import com.usoft.framework.bi.api.DataSetExeResponse;
import com.usoft.framework.bi.api.DatasetCreateRequest;
import com.usoft.framework.bi.api.DatasetFolderCreateRequest;
import com.usoft.framework.bi.api.DatasetFolderUpdateRequest;
import com.usoft.framework.bi.api.DatasetQueryRequest;
import com.usoft.framework.bi.api.DatasetResponse;
import com.usoft.framework.bi.api.DatasetUpdateRequest;
import com.usoft.framework.bi.api.GenerateSqlRequest;
import com.usoft.framework.bi.api.GenerateSqlResponse;
import com.usoft.framework.bi.api.TestSqlRequest;
import com.usoft.framework.bi.entity.DatasetFolderEntity;
import com.usoft.framework.bi.service.DataSetService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bi/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@AuthorizeDescriptionGroup({
        @AuthorizeDescription(value = "数据集管理", authorize = "tenant:sa:dataset", group = true),
        @AuthorizeDescription(value = "数据集文件夹管理", authorize = "tenant:sa:dataset-folder", group = true),
})
public class DataSetController {

    private final DataSetService biDataSetService;

    // --- Folder API ---

    @PostMapping("/folders")
    @AuthorizeDescription("创建数据集文件夹")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset-folder:create')")
    public ApiResponse<DatasetFolderEntity> createFolder(@Validated @RequestBody DatasetFolderCreateRequest req) {
        return ApiResponse.ok(biDataSetService.createFolder(req));
    }

    @PutMapping("/folders/{id}")
    @AuthorizeDescription("更新数据集文件夹")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset-folder:update')")
    public ApiResponse<DatasetFolderEntity> updateFolder(@PathVariable String id,
            @Validated @RequestBody DatasetFolderUpdateRequest req) {
        return ApiResponse.ok(biDataSetService.updateFolder(id, req));
    }

    @DeleteMapping("/folders/{id}")
    @AuthorizeDescription("删除数据集文件夹")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset-folder:delete')")
    public ApiResponse<Boolean> deleteFolder(@PathVariable String id) {
        return ApiResponse.ok(biDataSetService.deleteFolder(id));
    }

    @GetMapping("/folders")
    @AuthorizeDescription("获取数据集文件夹列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset-folder:list')")
    public ApiResponse<List<DatasetFolderEntity>> listFolders() {
        return ApiResponse.ok(biDataSetService.listFolders());
    }

    // --- Dataset API ---

    @PostMapping
    @AuthorizeDescription("创建数据集")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:create')")
    public ApiResponse<DatasetResponse> createDataset(@Validated @RequestBody DatasetCreateRequest req) {
        return ApiResponse.ok(biDataSetService.createDataset(req));
    }

    @PutMapping("/{id}")
    @AuthorizeDescription("更新数据集")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:update')")
    public ApiResponse<DatasetResponse> updateDataset(@PathVariable String id,
            @Validated @RequestBody DatasetUpdateRequest req) {
        return ApiResponse.ok(biDataSetService.updateDataset(id, req));
    }

    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除数据集")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:delete')")
    public ApiResponse<Boolean> deleteDataset(@PathVariable String id) {
        return ApiResponse.ok(biDataSetService.deleteDataset(id));
    }

    @GetMapping("/{id}")
    @AuthorizeDescription("获取数据集详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:get')")
    public ApiResponse<DatasetResponse> getDataset(@PathVariable String id) {
        return ApiResponse.ok(biDataSetService.getDataset(id));
    }

    @GetMapping
    @AuthorizeDescription("获取数据集列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:list')")
    public ApiResponse<PageResponse<DatasetResponse>> listDatasets(@Validated DatasetQueryRequest req) {
        return ApiResponse.ok(biDataSetService.listDatasets(req));
    }

    @PostMapping("/generate-sql")
    @AuthorizeDescription("数据集-生成SQL")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:generate-sql')")
    public ApiResponse<GenerateSqlResponse> generateSql(@RequestBody @Validated GenerateSqlRequest req) {
        return ApiResponse.ok(biDataSetService.generateSql(req));
    }

    /**
     * 助手聊天
     */
    @PostMapping("/assistant-chat")
    @AuthorizeDescription("数据集-助手聊天")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:assistant-chat')")
    public ApiResponse<DatasetResponse> assistantChat(@RequestBody @Validated DataSetAssistantChatRequest req) {
        return ApiResponse.ok(biDataSetService.assistantChat(req));
    }

    /**
     * 测试SQL
     */
    @PostMapping("/test-sql")
    @AuthorizeDescription("数据集-测试SQL")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:test-sql')")
    public ApiResponse<List<Map<String, Object>>> testSql(@RequestBody @Validated TestSqlRequest req) {
        return ApiResponse.ok(biDataSetService.testSql(req));
    }

    /**
     * 执行数据集
     */
    @PostMapping("/execute/{id}")
    @AuthorizeDescription("执行数据集")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:dataset:execute')")
    public ApiResponse<DataSetExeResponse> execute(@PathVariable String id,
            @RequestParam(required = false) String aiPrompt,
            @RequestBody @Validated Map<String, Object> params) {
        return ApiResponse.ok(biDataSetService.execute(id, aiPrompt, params));
    }
}
