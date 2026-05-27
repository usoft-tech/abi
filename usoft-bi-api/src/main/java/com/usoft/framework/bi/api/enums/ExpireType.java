package com.usoft.framework.bi.api.enums;

import com.mybatisflex.annotation.EnumValue;

public enum ExpireType {

    ONE_DAY("1d"),
    SEVEN_DAY("7d"),
    THIRTY_DAY("30d"),
    CUSTOM("custom"),
    PERMANENT("permanent");

    private final String value;

    ExpireType(String value) {
        this.value = value;
    }

    @EnumValue
    public String getValue() {
        return value;
    }
}
