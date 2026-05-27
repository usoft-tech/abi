package com.usoft.framework.bi.api;

import java.time.Instant;
import java.util.List;

import com.usoft.framework.bi.api.BatchUpdateAuthRequest.AuthItem;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.ExpireType;

import lombok.Data;

@Data
public class ShareCreateRequest {
    private BizType bizType;
    private String bizId;
    private ExpireType expireType;
    private Instant expireAt;
    private List<AuthItem> authorizations;
}
