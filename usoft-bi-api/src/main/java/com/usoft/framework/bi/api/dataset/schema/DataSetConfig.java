package com.usoft.framework.bi.api.dataset.schema;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 数据集配置
 */
@Data
public class DataSetConfig {

    /**
     * API调用描述
     */
    private Api api;
    /**
     * SQL语句
     */
    private Sql sql;

    /**
     * 输入字段描述
     */
    private Input input;
    /**
     * 输出字段描述
     */
    private Output output;

    /**
     * 脚本
     */
    private String script;

    /**
     * API调用容器
     */
    @Data
    public static class Api {
        private String url;
        /**
         * 请求方法
         */
        private String method;
        /**
         * 请求头
         */
        private Map<String, String> headers;
        /**
         * 请求参数
         */
        private Map<String, String> params;
        /**
         * 请求体
         */
        private String body;
    }

    /**
     * SQL语句容器
     */
    @Data
    public static class Sql {
        /**
         * SQL语句
         */
        private String sql;
        /**
         * 数据源ID
         */
        private String dataSourceId;
    }

    /**
     * 输入字段描述
     */
    @Data
    public static class Input {
        /**
         * 输入字段列表
         */
        private List<InputField> fields;
    }

    /**
     * 输入字段描述
     */
    @Data
    public static class InputField {
        /**
         * 输入字段key
         */
        private String key;
        /**
         * 字段类型
         */
        private FieldType type;
        /**
         * 是否必填
         */
        private Boolean required;
    }

    /**
     * 输出字段描述
     */
    @Data
    public static class Output {
        /**
         * 输出字段列表
         */
        private List<OutputField> fields;
    }

    /**
     * 输出字段描述
     */
    @Data
    public static class OutputField {
        /**
         * 字段key
         */
        private String key;
        /**
         * 字段名称
         */
        private String name;
        /**
         * 字段类型
         */
        private FieldType type;
        /**
         * 子字段schema(仅当type为OBJECT或ARRAY时有效)
         */
        private Output schema;
    }

    public enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        OBJECT,
        ARRAY
    }
}
