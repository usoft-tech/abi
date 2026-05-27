package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;
import com.usoft.framework.system.api.enums.TenantAuthKey;

import lombok.Data;

@Data
@Table("sys_user_tenant")
public class UserTenantEntity {
    private String userId;
    private String tenantId;
    private TenantAuthKey authKey;
}
