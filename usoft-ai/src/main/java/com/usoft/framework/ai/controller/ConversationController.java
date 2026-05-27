package com.usoft.framework.ai.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.ai.api.ConversationQueryRequest;
import com.usoft.framework.ai.api.ConversationResponse;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.ai.service.ConversationService;

@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "AI管理", authorize = "ai", group = true),
    @AuthorizeDescription(value = "AI会话", authorize = "ai:conversation", group = true),
})
@RestController
@RequestMapping(value = "/api/ai/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription(value = "删除会话", authorize = "ai:conversation:delete")
    @PreAuthorize("@ss.hasAuthority('ai:conversation:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(conversationService.delete(id));
    }

    /**
     * 查询会话列表
     */
    @GetMapping
    @AuthorizeDescription(value = "查询会话列表", authorize = "ai:conversation:list")
    @PreAuthorize("@ss.hasAuthority('ai:conversation:list')")
    public ApiResponse<List<ConversationResponse>> list(@Validated ConversationQueryRequest req) {
        return ApiResponse.ok(conversationService.list(req));
    }

    /**
     * 激活会话
     */
    @PostMapping("/{id}/activate")
    @AuthorizeDescription(value = "激活会话", authorize = "ai:conversation:activate")
    @PreAuthorize("@ss.hasAuthority('ai:conversation:activate')")
    public ApiResponse<Boolean> activate(@PathVariable String id) {
        return ApiResponse.ok(conversationService.activate(id));
    }
}
