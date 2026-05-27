package com.usoft.framework.ai.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.usoft.framework.common.annotation.AuthorizeDescription;
import com.usoft.framework.common.annotation.AuthorizeDescriptionGroup;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.ai.api.ConversationMessageQueryRequest;
import com.usoft.framework.ai.api.ConversationMessageResponse;
import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.ai.service.ConversationMessageService;

@RestController
@RequestMapping(value = "/api/ai/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@AuthorizeDescriptionGroup({
    @AuthorizeDescription(value = "会话消息", authorize = "ai:conversation:message", group = true)
})
public class ConversationMessageController {
    private final ConversationMessageService messageService;

    public ConversationMessageController(ConversationMessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 删除会话消息
     */
    @DeleteMapping("/{id}")
    @AuthorizeDescription(value = "删除会话消息", authorize = "ai:conversation:message:delete")
    @PreAuthorize("@ss.hasAuthority('ai:conversation:message:delete')")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(messageService.delete(id));
    }

    /**
     * 分页查询会话消息
     */
    @GetMapping
    @AuthorizeDescription(value = "查询会话消息", authorize = "ai:conversation:message:list")
    @PreAuthorize("@ss.hasAuthority('ai:conversation:message:list')")
    public ApiResponse<List<ConversationMessageResponse>> list(@Validated ConversationMessageQueryRequest req) {
        List<ConversationMessageResponse> list = new ArrayList<>(messageService.list(req));
        Collections.reverse(list);
        return ApiResponse.ok(list);
    }
}
