package com.usoft.framework.bi.api;

import java.util.List;

import com.usoft.framework.bi.api.enums.AuthorizationScope;
import com.usoft.framework.bi.api.enums.BizType;

import lombok.Data;

@Data
public class BatchUpdateAuthRequest {
    private String bizId;
    private BizType bizType;
    private List<AuthItem> authorizations;

    @Data
    public static class AuthItem {
        private AuthorizationScope scope;
        private String authorizerId;
    }
}
