package com.usoft.framework.system.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户端凭证创建请求体
 */
@Data
public class SysAuthClientCreateRequest {
    @NotBlank(message = "客户端名称不能为空")
    private String name;
    private String description;
    private Instant expiredAt;
}
