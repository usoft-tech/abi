package com.usoft.framework.system.api.dto;

import lombok.Data;

@Data
public class TenantSiteConfig {

    private String logo;
    private String name;
    private LogoType logoType;
    private String description;
    private String keywords;
    private String copyright;

    public enum LogoType {
        LOGO_NAME,
        ONLY_LOGO,
        ONLY_NAME;
    }
}
