package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.dataset.schema.DataSetConfig;
import com.usoft.framework.bi.api.enums.DataSetType;

import lombok.Data;

@Data
public class DatasetCreateRequest {
    private String folderId;
    private String name;
    private DataSetType type;
    private DataSetConfig config;
    private String description;
}
