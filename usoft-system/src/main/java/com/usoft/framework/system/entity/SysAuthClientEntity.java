package com.usoft.framework.system.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import lombok.Data;

/**
 * 客户端凭证实体
 */
@Data
@Table("sys_auth_client")
public class SysAuthClientEntity {
    @Id
    private String tenantId;
    @Id
    private String clientId;
    private String name;
    private String description;
    private String clientSecret;
    private Instant expiredAt;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
