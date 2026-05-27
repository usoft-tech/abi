package com.usoft.framework.bi.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新页面请求
 */
@Data
public class PageUpdateRequest {
    @NotBlank(message = "页面名称不能为空")
    private String name;
    private String description;

    /**
     * 行业
     */
    private String industry;
    private String status;
    private String cover;
}

