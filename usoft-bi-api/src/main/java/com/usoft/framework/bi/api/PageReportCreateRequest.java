package com.usoft.framework.bi.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建页面报告请求
 */
@Data
public class PageReportCreateRequest {
    private String id;
    /**
     * 页面ID
     */
    @NotBlank(message = "页面ID不能为空")
    private String pageId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    private String content;
}
