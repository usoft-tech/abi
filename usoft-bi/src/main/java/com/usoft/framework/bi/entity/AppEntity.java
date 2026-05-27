package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.bi.api.enums.AppMenuType;

import lombok.Data;

@Data
@Table("bi_app")
public class AppEntity {
    @Id
    private String id;
    private String tenantId;
    private AppMenuType menuType;
    private String title;
    private String icon;
    private String pageId;
    private String appKey;
    private String redirectUrl;
    private String parentId;
    private String ancestorIds;
    private Integer sort;
    private String ancestorSorts;
    private Integer level;
    private Boolean isLeaf;
    private Boolean hasWatermark;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}

