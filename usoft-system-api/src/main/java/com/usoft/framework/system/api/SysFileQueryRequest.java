package com.usoft.framework.system.api;

import lombok.Data;

@Data
public class SysFileQueryRequest {
    private int page;
    private int size;
    private String name;
    private String contentType;
    private String keyword;
}
