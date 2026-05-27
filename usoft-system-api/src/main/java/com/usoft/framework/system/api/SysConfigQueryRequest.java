package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

@Data
public class SysConfigQueryRequest extends PageRequest {
    private String configKey;
    private String keyword;
}
