package com.usoft.framework.bi.api;

import lombok.Data;

/**
 * 页面报告响应
 */
@Data
public class PageReportResponse {
    /**
     * ID
     */
    private String id;

    /**
     * 页面ID
     */
    private String pageId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private java.time.Instant createdAt;
}
