package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

import com.mybatisflex.annotation.Id;

/**
 * 用户实体
 */
@Data
@Table("sys_user")
public class UserEntity {
    @Id
    private String id;
    private String username;
    private String password;
    private String departmentId;
    private String employeeNo;
    private String displayName;
    private String avatar;
    private EnableStatus status;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;

}
