package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.PageSchema;
import lombok.Data;

/**
 * 页面模板响应体
 */
@Data
public class PageTemplateResponse {
    private String id;
    private String name;
    private String description;
    private String cover;
    private PageSchema schema;
}
