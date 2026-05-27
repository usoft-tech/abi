package com.usoft.framework.core.tenant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.mybatisflex.annotation.Table;

/**
 * 租户实体扫描器
 * <p>
 * 启动时扫描带有 @Table 注解且包含 tenantId 字段的类，并缓存到全局变量中
 */
@Component
public class TenantEntityScanner implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TenantEntityScanner.class);

    @Autowired
    private ApplicationContext applicationContext;

    // 缓存具有 tenantId 的表名
    private static final Set<String> TENANT_TABLES = new HashSet<>();

    /**
     * 获取所有租户表名
     */
    public static Set<String> getTenantTables() {
        return Collections.unmodifiableSet(TENANT_TABLES);
    }

    /**
     * 判断是否为租户表
     */
    public static boolean isTenantTable(String tableName) {
        return TENANT_TABLES.stream().anyMatch(tableName::equalsIgnoreCase);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Starting TenantEntityScanner...");
        long start = System.currentTimeMillis();

        // 创建扫描器，禁用默认过滤器（默认只扫描 @Component）
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        // 添加包含过滤器：只扫描 @Table 注解的类
        provider.addIncludeFilter(new AnnotationTypeFilter(Table.class));

        List<String> packages = new ArrayList<>();
        try {
            // 1. 尝试从 @SpringBootApplication 注解获取 scanBasePackages
            Map<String, Object> annotatedBeans = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
            for (Object bean : annotatedBeans.values()) {
                Class<?> userClass = ClassUtils.getUserClass(bean);
                SpringBootApplication annotation = userClass.getAnnotation(SpringBootApplication.class);
                if (annotation != null && annotation.scanBasePackages().length > 0) {
                    packages.addAll(Arrays.asList(annotation.scanBasePackages()));
                }
            }

            // 2. 如果没有获取到，尝试使用 AutoConfigurationPackages
            if (packages.isEmpty()) {
                packages.addAll(AutoConfigurationPackages.get(applicationContext));
            }
        } catch (Exception e) {
            logger.warn("Could not determine packages to scan, using default 'com.usoft.framework'", e);
        }

        // 3. 默认兜底
        if (packages.isEmpty()) {
            packages.add("com.usoft.framework");
        }

        for (String basePackage : packages) {
            // 扫描包
            Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);

            for (BeanDefinition component : components) {
                try {
                    String className = component.getBeanClassName();
                    if (className != null) {
                        Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
                        if (hasTenantIdField(clazz)) {
                            Table table = clazz.getAnnotation(Table.class);
                            if (table != null) {
                                TENANT_TABLES.add(table.value());
                                logger.debug("Found tenant entity: {}, table: {}", clazz.getName(), table.value());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to load class: " + component.getBeanClassName(), e);
                }
            }
        }

        logger.info("TenantEntityScanner completed in {} ms. Found {} tenant entities.",
                System.currentTimeMillis() - start, TENANT_TABLES.size());
    }

    /**
     * 检查类及其父类中是否存在 tenantId 字段
     */
    private boolean hasTenantIdField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField("tenantId");
                // 简单的类型检查，通常 tenantId 是 String 或 Long
                if (field.getType() == String.class || field.getType() == Long.class) {
                    return true;
                }
            } catch (NoSuchFieldException e) {
                // 当前类没有该字段，继续检查父类
            }
            current = current.getSuperclass();
        }
        return false;
    }
}
