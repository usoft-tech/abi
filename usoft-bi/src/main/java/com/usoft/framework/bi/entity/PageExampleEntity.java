package com.usoft.framework.bi.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.time.Instant;

/**
 * 页面片段实体类
 */
@Data
@Table("bi_page_example")
public class PageExampleEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String description;
    private String cover;
    private String itemJson;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
