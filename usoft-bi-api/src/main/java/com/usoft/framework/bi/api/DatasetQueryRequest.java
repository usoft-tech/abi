package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.enums.DataSetType;
import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DatasetQueryRequest extends PageRequest {
    private String folderId;
    private String datasourceId;
    private String name;
    private DataSetType type;
    private EnableStatus status;
}
