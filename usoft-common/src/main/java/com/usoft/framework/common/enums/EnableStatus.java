package com.usoft.framework.common.enums;

import lombok.Getter;

@Getter
public enum EnableStatus {
    ENABLE("启用"),
    DISABLE("禁用");

    private final String label;

    EnableStatus(String label) {
        this.label = label;
    }
}
