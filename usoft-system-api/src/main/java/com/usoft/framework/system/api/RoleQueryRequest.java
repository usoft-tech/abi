package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

@Data
public class RoleQueryRequest extends PageRequest {
    private String name;
    private String code;
    private String keyword;
}
