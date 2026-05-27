package com.usoft.framework.bi.api;

import com.usoft.framework.common.api.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DatasourceFieldQueryRequest extends PageRequest {
    private String datasourceId;
    private String dbId;
    private String tableId;
    private String name;
}
