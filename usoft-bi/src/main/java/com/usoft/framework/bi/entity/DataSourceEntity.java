package com.usoft.framework.bi.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;

@Data
@Table("bi_datasource")
public class DataSourceEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String type;
    private String driverClassName;
    private String url;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String extProps;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}

