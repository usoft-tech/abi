package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.system.api.enums.PermissionType;

import lombok.Data;
import java.time.Instant;

/**
 * 权限实体
 */
@Data
@Table("sys_permission")
public class SysPermissionEntity {
    @Id
    private String id;
    private String parentId;
    private String name;
    private String code;
    private PermissionType type; // group, perm
    private String description;
    private Integer sort;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
