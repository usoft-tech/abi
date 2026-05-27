package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 系统图标实体
 */
@Data
@Table("sys_icon")
public class SysIconEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String svg;
    private String description;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}

