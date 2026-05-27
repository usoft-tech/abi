package com.usoft.framework.security.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客户端登录请求体
 */
@Data
public class SysAuthClientLoginRequest {
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;
    @NotBlank(message = "客户端密钥不能为空")
    private String clientSecret;
}
