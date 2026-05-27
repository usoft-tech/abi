package com.usoft.framework.ai.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Id;
import lombok.Data;

@Data
@Table("ai_conversation_message")
public class ConversationMessageEntity {
    @Id
    private String id;
    private String conversationId;
    private String question;
    private String prompt;
    private String answer;
    private String files;
    private String agents;
    private String atItems;
    private String extraProps;
    private String tenantId;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
}

