package com.usoft.framework.ai.api;

import java.util.List;

import lombok.Data;

/**
 * 创建会话消息请求
 */
@Data
public  class ConversationMessageCreateRequest {
    private String id;
    private String conversationId;
    private String question;
    private String prompt;
    private List<ChatMessageFile> files;
    private List<ChatMessageAgent> agents;
    private String atItems;
    private String extra;
    private String answer;
}

