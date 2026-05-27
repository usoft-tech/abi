package com.usoft.framework.bi.api;

import lombok.Data;

@Data
public class DatasetFolderCreateRequest {
    private String name;
    private String parentId;
    private Integer sort;
}
