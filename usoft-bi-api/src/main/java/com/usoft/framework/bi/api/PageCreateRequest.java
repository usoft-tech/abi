package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.enums.PageStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建页面请求
 */
@Data
public class PageCreateRequest {
    @NotBlank(message = "页面名称不能为空")
    private String name;
    private String description;

    /**
     * 行业
     */
    private String industry;
    private PageStatus status;
    private String cover;
}

