package com.usoft.framework.system.controller;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.usoft.framework.system.api.SysFileResponse;
import com.usoft.framework.system.service.SysFileService;
import com.usoft.framework.common.api.ApiResponse;

@RestController
@RequestMapping(value = "/api/files", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "文件管理", authorize = "file")
public class SysFileController {

    private final SysFileService sysFileService;

    public SysFileController(SysFileService sysFileService) {
        this.sysFileService = sysFileService;
    }

    /**
     * 上传文件
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthorizeDescription("上传文件")
    @PreAuthorize("@ss.hasAuthority('file:upload')")
    public ApiResponse<SysFileResponse> upload(@RequestParam MultipartFile file, @RequestParam String type) {
        return ApiResponse.ok(sysFileService.create(file, type));
    }
}
