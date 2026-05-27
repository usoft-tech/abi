package com.usoft.framework.system.api.enums;

import lombok.Getter;

@Getter
public enum TenantAuthKey {

    ADMIN("管理员"),
    USER("用户");

    private final String label;

    TenantAuthKey(String label) {
        this.label = label;
    }
}
