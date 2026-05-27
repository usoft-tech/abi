package com.usoft.framework.bi.api;

import com.usoft.framework.common.api.PageRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建页面报告请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageReportQueryRequest extends PageRequest {
    /**
     * 页面ID
     */
    @NotBlank(message = "页面ID不能为空")
    private String pageId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
