package com.usoft.framework.bi.api;

import java.util.List;

import com.usoft.framework.bi.api.enums.PageStatus;
import com.usoft.framework.bi.api.page.schema.PageSchema;

import lombok.Data;

/**
 * 页面响应体
 */
@Data
public class PageResponse {
    private String id;
    private String name;
    private String description;

    /**
     * 行业
     */
    private String industry;
    private PageSchema schema;
    private Boolean hasWatermark;
    private PageStatus status;
    private String cover;
    private List<String> dbIds;
    private List<String> excelIds;
    private List<File> excels;

    
    @Data
    public static class File {
        private String id;
        private String name;
        private String extension;
    }
}

