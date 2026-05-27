package com.usoft.framework.system.api;

import java.time.Instant;

import lombok.Data;

/**
 * 客户端凭证响应体
 */
@Data
public class SysAuthClientResponse {
    private String tenantId;
    private String clientId;
    private String name;
    private String description;
    private String clientSecret;
    private Instant expiredAt;
}
