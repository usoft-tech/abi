package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;

/**
 * 系统配置实体
 */
@Data
@Table("sys_config")
public class SysConfigEntity {
    @Id
    private String id;
    private String tenantId;
    private String configKey;
    private String configValue;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;

}
