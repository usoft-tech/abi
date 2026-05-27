package com.usoft.framework.system.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建请求体
 */
@Data
public class RoleCreateRequest {
    @NotBlank(message = "角色名称不能为空")
    private String name;
    @NotBlank(message = "角色编码不能为空")
    private String code;
}
