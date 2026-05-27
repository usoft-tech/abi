package com.usoft.framework.bi.api;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.usoft.framework.bi.api.BatchUpdateAuthRequest.AuthItem;
import com.usoft.framework.bi.api.enums.BizType;
import com.usoft.framework.bi.api.enums.ExpireType;

import lombok.Data;

@Data
public class ShareResponse {
    private BizType bizType;
    private String bizId;
    private ExpireType expireType;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Instant expireAt;
    private String shareKey;
    private List<AuthItem> authorizations;
}
