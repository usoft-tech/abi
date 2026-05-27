package com.usoft.framework.bi.api.enums;

import lombok.Getter;

@Getter
public enum AppMenuType {

    /**
     * 目录
     */
    GROUP("目录"),
    /**
     * 菜单
     */
    PAGE("菜单");

    private final String label;

    AppMenuType(String label) {
        this.label = label;
    }
}
