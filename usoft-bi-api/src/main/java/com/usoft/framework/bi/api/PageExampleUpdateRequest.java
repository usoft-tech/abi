package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.SchemaItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新页面片段请求
 */
@Data
public class PageExampleUpdateRequest {
    @NotBlank(message = "ID 不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    private String name;
    private String description;
    private String cover;
    private SchemaItem item;
}
