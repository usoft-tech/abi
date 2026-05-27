package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
@Table("bi_datasource_db")
public class DatasourceDbEntity {
    @Id
    private String id;
    private String tenantId;
    private String datasourceId;
    private String name;
    private String type;
    private String dbid;
    private String description;
    private EnableStatus status;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}

