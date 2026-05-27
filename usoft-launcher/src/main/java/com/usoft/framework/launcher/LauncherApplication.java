package com.usoft.framework.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 应用启动入口
 */
@SpringBootApplication(scanBasePackages = "com.usoft.framework")
@MapperScan(basePackages = "com.usoft.framework", annotationClass = Mapper.class)
public class LauncherApplication {

    /**
     * 启动应用
     */
    public static void main(String[] args) {
        SpringApplication.run(LauncherApplication.class, args);
    }
}
