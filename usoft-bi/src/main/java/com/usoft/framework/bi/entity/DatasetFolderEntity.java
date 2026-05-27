package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import lombok.Data;

@Data
@Table("bi_dataset_folder")
public class DatasetFolderEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String parentId;
    private String ancestorIds;
    private Integer sort;
    private String ancestorSorts;
    private Integer level;
    private Boolean isLeaf;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
