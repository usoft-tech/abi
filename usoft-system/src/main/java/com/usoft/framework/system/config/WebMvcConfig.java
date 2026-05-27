package com.usoft.framework.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

// @Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final UploadProperties uploadProperties;

    public WebMvcConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    /**
     * 映射上传文件的访问路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String storage = uploadProperties.getStoragePath();
        if (storage == null || storage.isBlank()) {
            storage = "uploads";
        }
        String absolute = Paths.get(storage).toAbsolutePath().toString();
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + ensureTrailingSlash(absolute));
    }

    private String ensureTrailingSlash(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            return path;
        }
        return path + "/";
    }
}
