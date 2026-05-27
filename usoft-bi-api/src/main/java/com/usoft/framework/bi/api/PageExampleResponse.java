package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.SchemaItem;
import lombok.Data;

/**
 * 页面片段响应体
 */
@Data
public class PageExampleResponse {
    private String id;
    private String name;
    private String description;
    private String cover;
    private SchemaItem item;
}
