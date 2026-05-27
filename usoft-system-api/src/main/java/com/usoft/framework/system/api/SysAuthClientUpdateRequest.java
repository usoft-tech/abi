package com.usoft.framework.system.api;

import java.time.Instant;

import lombok.Data;

/**
 * 客户端凭证更新请求体
 */
@Data
public class SysAuthClientUpdateRequest {
    private String name;
    private String description;
    private Instant expiredAt;
}
