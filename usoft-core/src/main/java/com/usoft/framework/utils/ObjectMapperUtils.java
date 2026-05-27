package com.usoft.framework.utils;

import java.io.Writer;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 对象映射工具类，基于 Jackson 库
 */
public class ObjectMapperUtils {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true);
        mapper.configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature(),
        // true);
        OBJECT_MAPPER = mapper;
    }

    private ObjectMapperUtils() {
        // 防止实例化
    }

    /**
     * 转换为 JSON 字符串
     */
    public static String toJson(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("转换为 JSON 字符串失败", e);
        }
    }

    /**
     * 从 JSON 字符串转换为对象
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            try {
                return JSON.parseObject(json, valueType);
            } catch (Exception ex) {
                throw new RuntimeException("从 JSON 字符串转换为对象失败", ex);
            }
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("从 JSON 字符串转换为对象失败", e);
        }
    }

    /**
     * 写入 JSON 字符串
     */
    public static void writeValue(Writer writer, Object object) {
        try {
            OBJECT_MAPPER.writeValue(writer, object);
        } catch (Exception e) {
            throw new RuntimeException("写入 JSON 字符串失败", e);
        }
    }
}
