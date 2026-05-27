package com.usoft.framework.bi.api.enums;

/**
 * 数据集类型
 */
public enum DataSetType {
    /**
     * SQL数据集
     */
    SQL("SQL数据集"),
    /**
     * API数据集
     */
    API("API数据集");

    private final String label;

    DataSetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
