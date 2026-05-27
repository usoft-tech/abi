package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.PageSchema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新页面模板请求
 */
@Data
public class PageTemplateUpdateRequest {
    @NotBlank(message = "ID 不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    private String name;
    private String description;
    private String cover;
    private PageSchema schema;
}
