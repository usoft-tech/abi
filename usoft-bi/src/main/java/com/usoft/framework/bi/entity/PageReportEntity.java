package com.usoft.framework.bi.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import lombok.Data;

/**
 * 页面报告实体类
 */
@Data
@Table("bi_page_report")
public class PageReportEntity {
    /**
     * ID
     */
    @Id
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

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
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private java.time.Instant createdAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private java.time.Instant updatedAt;

    /**
     * 更新人
     */
    private String updatedBy;
}
