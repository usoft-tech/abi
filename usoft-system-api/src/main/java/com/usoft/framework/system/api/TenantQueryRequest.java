package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
public class TenantQueryRequest extends PageRequest {
    private String code;
    private String name;
    private EnableStatus status;
    private String keyword;
}
