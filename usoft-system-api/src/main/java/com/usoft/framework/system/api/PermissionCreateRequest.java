package com.usoft.framework.system.api;

import com.usoft.framework.system.api.enums.PermissionType;

import lombok.Data;

@Data
public class PermissionCreateRequest {
    private String parentId;
    private String name;
    private String code;
    private PermissionType type; // group, perm
    private String description;
    private Integer sort;
}
