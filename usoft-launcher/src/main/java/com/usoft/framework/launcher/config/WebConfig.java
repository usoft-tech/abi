package com.usoft.framework.launcher.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.usoft.framework.system.config.UploadProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final UploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String storage = uploadProperties.getStoragePath();
        if (storage == null || storage.isBlank()) {
            storage = "uploads";
        }
        String absolute = Paths.get(storage).toAbsolutePath().toString();
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + ensureTrailingSlash(absolute));

        registry.addResourceHandler("/webapp/**").addResourceLocations("classpath:/webapp/");

        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/webapp/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("").setViewName("redirect:/webapp/");
        registry.addViewController("/").setViewName("redirect:/webapp/");
        registry.addViewController("/webapp").setViewName("forward:/webapp/index.html");
        registry.addViewController("/webapp/").setViewName("forward:/webapp/index.html");
        registry.addViewController("/webapp/**/{path:[^\\\\.]*}").setViewName("forward:/webapp/index.html");
    }

    private String ensureTrailingSlash(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            return path;
        }
        return path + "/";
    }
}
