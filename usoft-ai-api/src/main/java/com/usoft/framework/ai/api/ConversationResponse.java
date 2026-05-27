package com.usoft.framework.ai.api;

import java.time.Instant;

import lombok.Data;

/**
 * 会话响应
 */
@Data
public class ConversationResponse {
    private String id;
    private String bizType;
    private String bizId;
    private String name;
    private Instant createdAt;
    private Boolean isActived;
}

