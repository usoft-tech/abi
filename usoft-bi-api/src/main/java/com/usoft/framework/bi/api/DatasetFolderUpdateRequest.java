package com.usoft.framework.bi.api;

import lombok.Data;

@Data
public class DatasetFolderUpdateRequest {
    private String name;
    private Integer sort;
    private String parentId;
}
