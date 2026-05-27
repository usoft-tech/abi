package com.usoft.framework.bi.entity;

import java.time.Instant;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.ExpireType;

import lombok.Data;

@Data
@Table("bi_share")
public class ShareEntity {
    @Id
    private String id;
    private BizType bizType;
    private String bizId;
    private String shareKey;
    private ExpireType expireType;
    private Instant expireAt;
    private Boolean isDeleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
