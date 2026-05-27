package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.SchemaItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建页面片段请求
 */
@Data
public class PageExampleCreateRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
    private String description;
    private String cover;
    private SchemaItem item;
}
