package com.usoft.framework.system.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统配置更新请求体
 */
@Data
public class SysConfigUpdateRequest {
    @NotBlank(message = "系统配置键不能为空")
    private String configKey;
    @NotBlank(message = "系统配置值不能为空")
    private String configValue;
}
