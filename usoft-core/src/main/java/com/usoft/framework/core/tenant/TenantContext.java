package com.usoft.framework.core.tenant;

/**
 * 租户上下文，基于ThreadLocal保存当前请求的租户ID
 */
public final class TenantContext {
    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return TENANT_HOLDER.get();
    }

    /**
     * 清理租户上下文
     */
    public static void clear() {
        TENANT_HOLDER.remove();
    }

    /**
     * 判断是否存在租户上下文
     */
    public static boolean hasTenant() {
        return TENANT_HOLDER.get() != null;
    }
}

