package com.usoft.framework.bi.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;

@Data
public class DataSourceQueryRequest extends PageRequest {
    private String name;
    private String type;
    private String keyword;

}

