package com.usoft.framework.system.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.usoft.framework.system.api.SysIconQueryRequest;
import com.usoft.framework.system.api.SysIconResponse;
import com.usoft.framework.system.service.SysIconService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.api.PageResponse;

/**
 * 系统图标控制器
 */
@Controller
@RequestMapping(value = "/api/icons")
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "图标管理", authorize = "tenant:sa:icon", group = true),
})
public class SysIconController {

    private final SysIconService sysIconService;

    /**
     * 构造控制器
     */
    public SysIconController(SysIconService sysIconService) {
        this.sysIconService = sysIconService;
    }

    /**
     * 上传SVG并创建图标
     * 
     * @param file        SVG文件
     * @param keepFill    是否保持填充
     * @param name        图标名称
     * @param description 图标描述
     * @return 图标响应
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthorizeDescription("上传图标")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:icon:upload')")
    public @ResponseBody ApiResponse<SysIconResponse> upload(@RequestParam("file") MultipartFile file,
            @RequestParam(name = "keepFill", defaultValue = "true") boolean keepFill,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "description", required = false) String description) {
        return ApiResponse.ok(sysIconService.upload(file, keepFill, name, description));
    }

    /**
     * 删除图标
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除图标")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:icon:delete')")
    public @ResponseBody ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(sysIconService.delete(id));
    }

    /**
     * 分页查询图标
     */
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @AuthorizeDescription("获取图标列表")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:icon:list')")
    public @ResponseBody ApiResponse<PageResponse<SysIconResponse>> iconfontString(SysIconQueryRequest req) {
        return ApiResponse.ok(sysIconService.list(req));
    }

    /**
     * 生成 Iconfont 风格的js
     */
    @GetMapping(path = "/{tenantId}.js", produces = "application/javascript")
    public ResponseEntity<String> js(@PathVariable String tenantId) {
        String js = sysIconService.iconfontString(tenantId);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .contentType(new MediaType("application", "javascript"))
                .body(js);
    }
}
