package com.usoft.framework.security.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户端刷新令牌请求体
 */
@Data
public class SysAuthClientRefreshRequest {
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
