package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import lombok.Data;

@Data
@Table("sys_role_permission")
public class SysRolePermissionEntity {

    @Id
    private String roleId;

    @Id
    private String permissionId;
}
