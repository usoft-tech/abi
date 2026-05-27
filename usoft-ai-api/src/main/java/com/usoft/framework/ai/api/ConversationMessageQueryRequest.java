package com.usoft.framework.ai.api;

import lombok.Data;

/**
 * 会话消息查询请求
 */
@Data
public class ConversationMessageQueryRequest {
    private String prevId;
    private Integer limit;
    private String conversationId;
    private String keyword;
}
