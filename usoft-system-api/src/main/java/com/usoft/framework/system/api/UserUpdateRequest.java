package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户更新请求体
 */
@Data
public class UserUpdateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    private String departmentId;
    private String employeeNo;
    private String displayName;
    private EnableStatus status;
}
