package com.usoft.framework.bi.api.enums;

/**
 * 授权业务类型
 */
public enum BizType {
    /**
     * 应用
     */
    APP("应用"),
    /**
     * 分享
     */
    SHARE("分享"),
    /**
     * 页面
     */
    PAGE("页面");

    private final String label;

    BizType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
