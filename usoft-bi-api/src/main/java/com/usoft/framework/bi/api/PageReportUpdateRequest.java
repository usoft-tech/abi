package com.usoft.framework.bi.api;

import lombok.Data;

/**
 * 更新页面报告请求
 */
@Data
public class PageReportUpdateRequest {
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
