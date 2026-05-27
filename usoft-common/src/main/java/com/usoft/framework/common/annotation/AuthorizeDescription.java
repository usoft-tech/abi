package com.usoft.framework.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 授权描述注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthorizeDescription {

    /**
     * 授权描述
     *
     * @return 授权描述
     */
    String value();
    /**
     * 授权表达式
     *
     * @return 授权表达式
     */
    String authorize() default "";

    /**
     * 是否为权限组
     *
     * @return 是否为权限组
     */
    boolean group() default false;
}
