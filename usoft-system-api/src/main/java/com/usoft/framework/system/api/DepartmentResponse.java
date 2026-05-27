package com.usoft.framework.system.api;
import lombok.Data;

/**
 * 部门响应体
 */
@Data
public class DepartmentResponse {
    private String id;
    private String name;
    private String parentId;
    private String ancestorIds;
    private Integer sort;
    private String ancestorSorts;
    private Integer level;
    private Boolean isLeaf;
}
