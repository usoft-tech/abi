package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.bi.api.enums.DataSetType;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
@Table("bi_dataset")
public class DatasetEntity {
    @Id
    private String id;
    private String tenantId;
    private String folderId;
    private String name;
    private DataSetType type;
    private String config;
    private String description;
    private EnableStatus status;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
