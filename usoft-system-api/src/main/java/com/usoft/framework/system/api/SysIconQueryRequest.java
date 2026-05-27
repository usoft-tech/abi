package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

/**
 * 图标查询请求体
 */
@Data
public class SysIconQueryRequest extends PageRequest {
    private String name;
    private String keyword;
}

