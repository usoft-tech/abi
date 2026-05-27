package com.usoft.framework.bi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.usoft.framework.YamlPropertySourceFactory;

/**
 * 配置加载器：加载 app 前缀下的 prompts 等配置
 * 通过 PropertySource 将 classpath:s-prompts.yaml 加入环境
 */
@Configuration
@PropertySource(value = "classpath:s-prompts.yml", factory = YamlPropertySourceFactory.class)
public class PromptConfig {

    /**
     * 加载 app.prompts 等配置为 PromptProperties Bean
     * @return PromptProperties
     */
    @Bean
    @ConfigurationProperties(prefix = "app")
    PromptProperties promptProperties() {
        return new PromptProperties();
    }
}
