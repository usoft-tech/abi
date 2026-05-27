package com.usoft.framework.ai.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Id;
import lombok.Data;

@Data
@Table("ai_conversation")
public class ConversationEntity {
    @Id
    private String id;
    private String bizType;
    private String bizId;
    private String name;
    private String tenantId;
    private Boolean isActived;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
}
