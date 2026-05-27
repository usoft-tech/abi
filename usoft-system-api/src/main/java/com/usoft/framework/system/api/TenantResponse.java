package com.usoft.framework.system.api;
import com.usoft.framework.system.api.dto.TenantSiteConfig;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
public class TenantResponse {
    private String id;
    private String code;
    private String name;
    private EnableStatus status;
    private String cover;
    private String description;
    private String contactUser;
    private String contactPhone;
    private TenantSiteConfig siteConfig;
}
