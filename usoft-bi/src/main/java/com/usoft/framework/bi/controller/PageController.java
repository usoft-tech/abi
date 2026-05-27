package com.usoft.framework.bi.controller;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.usoft.framework.bi.api.PageAssistantChatRequest;
import com.usoft.framework.bi.api.PageAssistantChatResponse;
import com.usoft.framework.bi.api.PageCreateRequest;
import com.usoft.framework.bi.api.PageQueryRequest;
import com.usoft.framework.bi.api.PageResponse;
import com.usoft.framework.bi.api.PageUpdateRequest;
import com.usoft.framework.bi.api.ShareCreateRequest;
import com.usoft.framework.bi.api.ShareResponse;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.ExpireType;
import com.usoft.framework.bi.api.page.schema.PageInterpretation;
import com.usoft.framework.bi.api.page.schema.PageSchema;
import com.usoft.framework.bi.entity.PageEntity;
import com.usoft.framework.bi.mapper.PageMapper;
import com.usoft.framework.bi.service.AuthorizationService;
import com.usoft.framework.bi.service.PageService;
import com.usoft.framework.bi.service.ShareService;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.exception.HttpStatusException;
import com.usoft.framework.common.sse.HeartbeatSseEmitter;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.service.TenantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/bi/pages", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "页面管理", authorize = "tenant:sa:page", group = true),
    @AuthorizeDescription(value = "页面分享", authorize = "tenant:sa:page:share", group = true),
})
public class PageController {
    private final PageService pageService;
    private final TenantService tenantService;
    private final ShareService shareService;
    private final AuthorizationService authorizationService;
    private final PageMapper pageMapper;

    /**
     * 创建页面
     */
    @PostMapping
    @AuthorizeDescription("创建页面")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:create')")
    public ApiResponse<PageResponse> create(@Validated @RequestBody PageCreateRequest req) {
        return ApiResponse.ok(pageService.create(req));
    }

    /**
     * 更新页面
     */
    @PutMapping("/{id}")
    @AuthorizeDescription("更新页面")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:update')")
    public ApiResponse<PageResponse> update(@PathVariable String id,
            @Validated @RequestBody PageUpdateRequest req) {
        return ApiResponse.ok(pageService.update(id, req));
    }

    /**
     * 更新页面设计
     */
    @PutMapping("/schema-{id}")
    @AuthorizeDescription("更新页面设计")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:update-schema')")
    public ApiResponse<PageResponse> updateSchema(@PathVariable String id,
            @Validated @RequestBody PageSchema req) {
        return ApiResponse.ok(pageService.updateSchema(id, req));
    }

    /**
     * 删除页面
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription("删除页面")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(pageService.delete(id));
    }

    /**
     * 获取页面详情
     */
    @GetMapping("/{id}")
    @AuthorizeDescription("获取页面详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:get')")
    public ApiResponse<PageResponse> get(@PathVariable String id) {
        return ApiResponse.ok(pageService.get(id));
    }

    /**
     * 分页查询页面
     */
    @GetMapping
    @AuthorizeDescription("分页查询页面")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:list')")
    public ApiResponse<com.usoft.framework.common.api.PageResponse<PageResponse>> list(
            @Validated PageQueryRequest req) {
        return ApiResponse.ok(pageService.list(req));
    }

    /**
     * 助手聊天
     */
    @PostMapping("/assistant-chat")
    @AuthorizeDescription("页面设计-助手聊天")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:assistant-chat')")
    public ApiResponse<PageAssistantChatResponse> assistantChat(
            @Validated @RequestBody PageAssistantChatRequest req) {
        return ApiResponse.ok(pageService.assistantChat(req));
    }

    /**
     * 获取页面分享详情
     */
    @GetMapping("/share/{id}")
    @AuthorizeDescription("获取页面分享详情")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:share:get')")
    public ApiResponse<ShareResponse> share(@PathVariable String id) {
        return ApiResponse.ok(shareService.getByBizId(BizType.PAGE, id));
    }

    /**
     * 创建页面分享
     */
    @PostMapping("/share")
    @AuthorizeDescription("创建页面分享")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:share:create')")
    public ApiResponse<ShareResponse> createShare(@Validated @RequestBody ShareCreateRequest req) {
        PageEntity page = pageMapper.selectOneById(req.getBizId());
        tenantService.checkTenantUserAuthKey(page.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        req.setBizType(BizType.PAGE);
        return ApiResponse.ok(shareService.create(req));
    }

    /**
     * 删除分享页面
     */
    @DeleteMapping("/share/{id}")
    @AuthorizeDescription("删除分享页面")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:share:delete')")
    public ApiResponse<Boolean> deleteShare(@PathVariable String id) {
        PageEntity page = pageMapper.selectOneById(id);
        tenantService.checkTenantUserAuthKey(page.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        return ApiResponse.ok(shareService.delete(BizType.PAGE, id));
    }

    /**
     * 获取分享页面详情
     */
    @GetMapping("/share-schema/{shareKey}")
    @AuthorizeDescription("获取分享页面详情")
    public ApiResponse<PageResponse> getSharePage(@PathVariable String shareKey) {
        authorizationService.checkAuth(null, BizType.SHARE, shareKey, UserHolder.userId());
        ShareResponse share = shareService.getByShareKey(BizType.PAGE, shareKey);
        if (share == null) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND.value(), "分享链接不存在");
        }
        ExpireType expireType = share.getExpireType();
        Instant expireAt = share.getExpireAt();
        if (isShareExpired(expireType, expireAt)) {
            throw new HttpStatusException(HttpStatus.GONE.value(), "分享链接已过期");
        }

        PageResponse page = pageService.get(share.getBizId());
        if (page == null) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND.value(), "页面不存在");
        }
        return ApiResponse.ok(page);
    }

    /**
     * 数据解读
     */
    @PostMapping(value = "/interpret/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthorizeDescription("数据解读")
    @PreAuthorize("@ss.hasAuthority('tenant:sa:page:assistant-interpret')")
    public SseEmitter interpret(@PathVariable String id,
            @RequestBody PageInterpretation requestBody,
            HttpServletRequest request,
            HttpServletResponse response) {
        HeartbeatSseEmitter<?> emitter = new HeartbeatSseEmitter<>(request.getRequestId());
        // Ensure SSE-friendly headers
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        // Prevent buffering on reverse proxies (e.g., Nginx)
        response.setHeader("X-Accel-Buffering", "no");

        // Send start event immediately
        emitter.send("start", null);

        String username = UserHolder.username();
        String tenantId = TenantContext.getTenantId();
        // Execute agent asynchronously to avoid blocking and enable streaming
        CompletableFuture.runAsync(() -> {
            try {
                pageService.interpret(requestBody, emitter, tenantId, username);
            } catch (Exception e) {
                log.error("sse async execute error", e);
                emitter.completeWithError(e);
            }
        });
        return emitter.getEmitter();
    }

    /**
     * 校验分享是否过期
     *
     * @param expireType 过期类型
     * @param expireAt   过期时间
     * @return true 表示已过期，false 表示未过期
     */
    private boolean isShareExpired(ExpireType expireType, Instant expireAt) {
        if (expireType == ExpireType.PERMANENT) {
            return false;
        }
        if (expireAt == null) {
            return true;
        }
        return Instant.now().isAfter(expireAt);
    }
}
