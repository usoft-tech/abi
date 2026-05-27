package com.usoft.framework.utils;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

public class SpringUtils {
    private static volatile ApplicationContext applicationContext;

    private SpringUtils() {
    }

    public static ApplicationContext getApplicationContext() {
        ApplicationContext ctx = applicationContext;
        if (ctx == null) {
            throw new IllegalStateException("Spring ApplicationContext 尚未初始化");
        }
        return ctx;
    }

    public static Environment getEnvironment() {
        return getApplicationContext().getEnvironment();
    }

    public static String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return getApplicationContext().getBean(name, requiredType);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getApplicationContext().getBeansOfType(type);
    }

    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    public static void publishEvent(Object event) {
        getApplicationContext().publishEvent(event);
    }

    public static void publishEvent(ApplicationEvent event) {
        getApplicationContext().publishEvent(event);
    }

    @Component
    static class ApplicationContextHolder implements ApplicationContextAware {
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            SpringUtils.applicationContext = applicationContext;
        }
    }
}
