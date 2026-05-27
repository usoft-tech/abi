package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户创建请求体
 */
@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String departmentId;
    private String employeeNo;
    private String displayName;
    private EnableStatus status;
}
