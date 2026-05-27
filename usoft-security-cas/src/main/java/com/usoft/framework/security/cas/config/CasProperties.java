package com.usoft.framework.security.cas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * CAS 配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "usoft.security.cas")
@Data
public class CasProperties {
    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * CAS 服务端 URL 前缀，例如：https://cas.example.com/cas
     */
    private String serverUrlPrefix;

    /**
     * CAS 服务端登录 URL，例如：https://cas.example.com/cas/login
     */
    private String serverLoginUrl;

    /**
     * CAS 服务端注销 URL，例如：https://cas.example.com/cas/logout
     */
    private String serverLogoutUrl;

    /**
     * 客户端主机 URL，例如：http://localhost:8080
     */
    private String clientHostUrl;
    
    /**
     * 客户端登录回调 URL 路径，例如：/api/auth/cas/callback
     */
    private String clientLoginUrl = "/api/auth/cas/callback";
    
    /**
     * 验证类型：cas 或 cas3
     */
    private String validationType = "cas3";
    
    /**
     * 前端回调地址，登录成功后跳转到前端页面，并携带 token
     */
    private String frontendCallbackUrl = "/third-auth/callback";
}
