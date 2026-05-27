package com.usoft.framework.system.api;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

/**
 * 用户响应体
 */
@Data
public class UserResponse {
    private String id;
    private String username;
    private String departmentId;
    private String employeeNo;
    private String displayName;
    private String avatar;
    private EnableStatus status;
}
