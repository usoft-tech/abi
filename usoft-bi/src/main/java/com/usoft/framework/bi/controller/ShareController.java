package com.usoft.framework.bi.controller;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.bi.api.ShareCreateRequest;
import com.usoft.framework.bi.api.ShareResponse;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.service.ShareService;
import com.usoft.framework.common.api.ApiResponse;

@RestController
@RequestMapping(value = "/api/bi/shares", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescription(value = "分享管理", authorize = "tenant:sa:share")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * 创建或更新分享
     */
    @PostMapping
    @AuthorizeDescription("创建或更新分享")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:share:save')")
    public ApiResponse<ShareResponse> create(@Validated @RequestBody ShareCreateRequest req) {
        return ApiResponse.ok(shareService.create(req));
    }

    /**
     * 根据业务ID获取分享信息
     */
    @GetMapping
    @AuthorizeDescription("根据业务ID获取分享信息")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:share:get')")
    public ApiResponse<ShareResponse> getByBizId(@RequestParam BizType bizType, @RequestParam String bizId) {
        return ApiResponse.ok(shareService.getByBizId(bizType, bizId));
    }
}
