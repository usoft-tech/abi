package com.usoft.framework.bi.api.enums;

/**
 * 授权范围
 */
public enum AuthorizationScope {
    /**
     * 公开
     */
    PUBLIC("公开"),
    /**
     * 租户所有人
     */
    TENANT("租户所有人"),
    /**
     * 指定用户
     */
    USER("指定用户");

    private final String label;

    AuthorizationScope(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
