package com.usoft.framework.system.api;

import com.usoft.framework.system.api.enums.PermissionType;

import lombok.Data;

@Data
public class PermissionResponse {
    private String id;
    private String parentId;
    private String name;
    private String code;
    private PermissionType type;
    private String description;
    private Integer sort;
    private Boolean builtIn;
}
