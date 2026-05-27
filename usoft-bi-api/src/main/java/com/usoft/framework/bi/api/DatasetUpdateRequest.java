package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.dataset.schema.DataSetConfig;
import com.usoft.framework.bi.api.enums.DataSetType;
import com.usoft.framework.common.enums.EnableStatus;

import lombok.Data;

@Data
public class DatasetUpdateRequest {
    private String folderId;
    private String name;
    private DataSetType type;
    private DataSetConfig config;
    private String description;
    private EnableStatus status;
}
