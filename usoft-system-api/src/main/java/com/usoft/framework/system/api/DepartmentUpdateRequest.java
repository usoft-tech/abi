package com.usoft.framework.system.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门更新请求体
 */
@Data
public class DepartmentUpdateRequest {
    @NotBlank(message = "部门名称不能为空")
    private String name;
    private String parentId;
    private Integer sort;
}
