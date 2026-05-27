package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端凭证查询请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAuthClientQueryRequest extends PageRequest {
    private String clientId;
    private String name;
    private String keyword;
}
