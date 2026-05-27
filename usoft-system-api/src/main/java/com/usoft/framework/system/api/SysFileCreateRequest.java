package com.usoft.framework.system.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysFileCreateRequest {
    @NotBlank(message = "文件名称不能为空")
    private String name;
    @NotBlank(message = "文件URL不能为空")
    private String url;
    private String contentType;
    private Long size;
}
