package com.usoft.framework.system.api;
import lombok.Data;

/**
 * 系统配置响应体
 */
@Data
public class SysConfigResponse {
    private String id;
    private String configKey;
    private String configValue;
}
