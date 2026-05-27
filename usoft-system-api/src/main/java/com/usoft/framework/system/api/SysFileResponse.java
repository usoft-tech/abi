package com.usoft.framework.system.api;
import lombok.Data;

@Data
public class SysFileResponse {
    private String id;
    private String name;
    private String url;
    private String contentType;
    private Long size;
    private String storagePath;
}

