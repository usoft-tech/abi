package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TenantUpdateRequest {
    @NotBlank(message = "租户编码不能为空")
    private String code;
    @NotBlank(message = "租户名称不能为空")
    private String name;
    @NotNull(message = "租户状态不能为空")
    private EnableStatus status;
    private String cover;
    private String description;
    private String contactUser;
    private String contactPhone;
}
