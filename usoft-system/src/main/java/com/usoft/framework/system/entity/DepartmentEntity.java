package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;

/**
 * 部门实体
 */
@Data
@Table("sys_department")
public class DepartmentEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String parentId;
    private String ancestorIds;
    private Integer sort;
    private String ancestorSorts;
    private Integer level;
    private Boolean isLeaf;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}
