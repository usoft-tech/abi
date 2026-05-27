package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("sys_user_role")
public class SysUserRoleEntity {
    @Id
    private String userId;
    @Id
    private String roleId;
}
