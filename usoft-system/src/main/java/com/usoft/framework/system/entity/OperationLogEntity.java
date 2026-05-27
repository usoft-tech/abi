package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;

import java.time.Instant;

/**
 * 操作日志实体
 */
@Data
@Table("sys_operation_log")
public class OperationLogEntity {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String username;
    private String tenantId;
    private String module;
    private String action;
    private String method;
    private String uri;
    private String status;
    private long durationMs;
    private String requestBody;
    private String responseBody;
    private Instant createdAt;
    private Boolean isDeleted;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

}
