package com.usoft.framework.bi.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.bi.api.enums.PageStatus;

import lombok.Data;

@Data
@Table("bi_page")
public class PageEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    /**
     * 描述
     */
    private String description;

    /**
     * 行业
     */
    private String industry;
    private String schemaJson;
    private PageStatus status;
    private String cover;
    private String dbIds;
    private String excelIds;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}

