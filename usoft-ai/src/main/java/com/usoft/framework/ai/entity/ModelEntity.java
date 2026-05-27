package com.usoft.framework.ai.entity;

import com.mybatisflex.annotation.Table;

import lombok.Data;

import com.mybatisflex.annotation.Id;

@Data
@Table("ai_model")
public class ModelEntity {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String provider;
    private String model;
    private String baseUrl;
    private String apiKey;
    private String extProps;
    private Boolean isDeleted;
    private java.time.Instant createdAt;
    private String createdBy;
    private java.time.Instant updatedAt;
    private String updatedBy;
}

