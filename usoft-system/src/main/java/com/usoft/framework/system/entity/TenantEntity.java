package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Table;
import com.usoft.framework.common.enums.EnableStatus;
import com.mybatisflex.annotation.Id;
import lombok.Data;

@Data
@Table("sys_tenant")
public class TenantEntity {
    @Id
    private String id;
    private String code;
    private String name;
    private EnableStatus status;
    private String cover;
    private String description;
    private String siteConfig;
    private String contactUser;
    private String contactPhone;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}
