package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.enums.AppMenuType;

import lombok.Data;

/**
 * 创建应用菜单请求
 */
@Data
public class AppCreateRequest {
    private AppMenuType menuType;
    private String title;
    private String icon;
    private String pageId;
    private String appKey;
    private String redirectUrl;
    private String parentId;
    private Integer sort;
    private Boolean hasWatermark;
}

