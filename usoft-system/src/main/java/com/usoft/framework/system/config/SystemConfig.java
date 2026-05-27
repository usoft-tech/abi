package com.usoft.framework.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConfig {

    /**
     * 上传配置
     */
    @Bean
    @ConfigurationProperties(prefix = "app.upload")
    UploadProperties uploadProperties() {
        return new UploadProperties();
    }
}
