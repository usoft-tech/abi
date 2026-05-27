package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

@Data
public class DepartmentQueryRequest extends PageRequest {
    private String name;
    private String parentId;
    private String keyword;
}
