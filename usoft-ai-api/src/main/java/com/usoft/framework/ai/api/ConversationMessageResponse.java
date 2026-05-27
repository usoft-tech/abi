package com.usoft.framework.ai.api;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 会话消息响应
 */
@Data
public class ConversationMessageResponse {
    private String id;

    private String conversationId;
    private String question;
    @JsonIgnore
    private String prompt;
    private List<ChatMessageFile> files;
    private List<ChatMessageAgent> agents;
    private String atItems;
    private String extra;
    private String answer;
    private Instant createdAt;
}
