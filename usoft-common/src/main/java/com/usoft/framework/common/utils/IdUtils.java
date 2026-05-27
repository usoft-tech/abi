package com.usoft.framework.common.utils;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 生成随机ID工具类
 */
public class IdUtils {
    private static final Supplier<String> ID_SUPPLIER = () -> UUID.randomUUID().toString();

    /**
     * 生成随机ID
     * @return 随机ID
     */
    public static String randomId() {
        return ID_SUPPLIER.get();
    }

    /**
     * 如果ID为空，则生成随机ID
     * @param id ID
     * @return ID或随机ID
     */
    public static String randomIfAbsent(String id) {
        return id != null ? id : randomId();
    }
}
