package com.usoft.framework.bi.api.page.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.Data;

@Data
public class InterpretationItem {
    private String id;
    private String type;
    private String name;
    private String description;

    private String data;
    private String script;
    private String template;

    @JsonDeserialize(using = ChidrenDeserializer.class)
    @JSONField(deserializeUsing = ChildrenObjectReader.class)
    private Object children;

    public static class ChidrenDeserializer extends StdDeserializer<Object> {

        public ChidrenDeserializer() {
            super(Object.class);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            if (p.isExpectedStartArrayToken()) {
                return ctxt.readValue(p,
                        ctxt.getTypeFactory().constructCollectionType(List.class, InterpretationItem.class));
            } else if (p.isExpectedStartObjectToken()) {
                return ctxt.readValue(p, ctxt.getTypeFactory().constructMapType(
                        Map.class,
                        ctxt.getTypeFactory().constructType(String.class),
                        ctxt.getTypeFactory().constructCollectionType(List.class, InterpretationItem.class)));
            } else {
                return null;
            }
        }
    }

    public static class ChildrenObjectReader implements com.alibaba.fastjson2.reader.ObjectReader<Object> {

        @Override
        public Object readObject(JSONReader reader, java.lang.reflect.Type fieldType, Object fieldName, long features) {
            if (reader.nextIfNull()) {
                return null;
            }
            if (reader.isArray()) {
                List<InterpretationItem> list = new ArrayList<>();
                reader.readArray(list, InterpretationItem.class);
                return list;
            }
            if (reader.isObject()) {
                Map<String, List<InterpretationItem>> children = new HashMap<>();
                reader.read(children, String.class,
                        new com.alibaba.fastjson2.TypeReference<List<InterpretationItem>>() {
                        }.getType(), features);
                return children;
            }
            return null;
        }
    }

}
