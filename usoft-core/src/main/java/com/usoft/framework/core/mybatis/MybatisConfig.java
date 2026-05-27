package com.usoft.framework.core.mybatis;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置，注册多租户拦截器
 */
@Configuration
public class MybatisConfig {

    /**
     * 注册多租户拦截器
     */
    @Bean
    public Interceptor multiTenantInterceptor() {
        return new MultiTenantInterceptor();
    }
}

