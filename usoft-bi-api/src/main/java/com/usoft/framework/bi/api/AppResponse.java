package com.usoft.framework.bi.api;

import java.util.List;

import com.usoft.framework.bi.api.enums.AppMenuType;

import lombok.Data;

/**
 * 应用菜单响应，结构匹配 React Router RouteObject
 */
@Data
public class AppResponse {
    private String id;
    private String path;
    private String parentId;
    private List<AppResponse> children;
    private Handle handle;

    private String tenantId;

    @Data
    public static class Handle {
        private AppMenuType type; // group, page (default)
        private String title;
        private String icon;
        private String pageId;
        private String appKey;
        private String redirectUrl;
        private Integer sort;
        private Boolean isLeaf;
        private Boolean hasWatermark;
    }
}
