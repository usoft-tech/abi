package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;

/**
 * 角色实体
 */
@Data
@Table("sys_role")
public class RoleEntity {
    @Id
    private String id;
    private String name;
    private String code;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;

}
