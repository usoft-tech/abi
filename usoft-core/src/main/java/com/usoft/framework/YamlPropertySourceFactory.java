package com.usoft.framework;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * YAML属性源工厂：支持通过 @PropertySource 加载 YAML 文件
 * 使用 Spring Boot 的 YamlPropertySourceLoader 解析 YAML
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * 创建属性源，支持 YAML 文件解析
     * @param name 属性源名称
     * @param resource 资源
     * @return PropertySource
     * @throws IOException 读取异常
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = name != null ? name : resource.getResource().getFilename();
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(sourceName, resource.getResource());
        return sources.isEmpty() ? null : sources.get(0);
    }
}
