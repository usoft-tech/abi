package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

@Data
public class UserQueryRequest extends PageRequest {
    private String username;
    private String departmentId;
    private String employeeNo;
    private String keyword;
}
