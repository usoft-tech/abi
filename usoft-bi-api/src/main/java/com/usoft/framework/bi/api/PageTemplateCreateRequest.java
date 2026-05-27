package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.PageSchema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建页面模板请求
 */
@Data
public class PageTemplateCreateRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
    private String description;
    private String cover;
    private PageSchema schema;
}
