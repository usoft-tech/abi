package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("sys_file")
public class SysFileEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String url;
    private String contentType;
    private Long size;
    private String storagePath;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}
