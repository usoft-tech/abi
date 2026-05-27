package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.AuthorizationScope;

import lombok.Data;

/**
 * 授权实体
 */
@Data
@Table("bi_authorization")
public class AuthorizationEntity {
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
     * 业务类型
     */
    private BizType bizType;
    /**
     * 业务ID
     */
    private String bizId;
    /**
     * 授权范围
     */
    private AuthorizationScope authorizerScope;
    /**
     * 授权者ID
     */
    private String authorizerId;
    /**
     * 是否删除
     */
    private Boolean isDeleted;
    /**
     * 创建时间
     */
    private Instant createdAt;
    /**
     * 创建人
     */
    private String createdBy;
    /**
     * 更新时间
     */
    private Instant updatedAt;
    /**
     * 更新人
     */
    private String updatedBy;
}
