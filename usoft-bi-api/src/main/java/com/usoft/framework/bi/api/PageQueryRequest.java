package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.enums.PageStatus;
import com.usoft.framework.common.api.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 页面查询请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PageQueryRequest extends PageRequest {
    private String name;
    private PageStatus status;
    private String keyword;
}

