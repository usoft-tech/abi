package com.usoft.framework.system.api;

import com.usoft.framework.system.api.dto.TenantSiteConfig;

import lombok.Data;

/**
 * 租户设置请求
 */
@Data
public class TenantSettingRequest {

    private String contactUser;
    private String contactPhone;

    private TenantSiteConfig siteConfig;
}
