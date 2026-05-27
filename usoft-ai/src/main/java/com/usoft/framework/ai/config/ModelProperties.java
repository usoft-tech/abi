package com.usoft.framework.ai.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import lombok.Data;

@Data
public class ModelProperties implements InitializingBean {
    private EmbeddingModel embeddingModel = new EmbeddingModel();
    private List<Model> models = new ArrayList<>();
    private Map<String, Model> modelMap = new HashMap<>();

    @Data
    public static class Model {

        private String name;
        private String provider;
        private Double temperature;
        private Integer maxTokens;
        private String apiBaseUrl;
        private String apiKey;
        private String model;
    }

    @Data
    public static class EmbeddingModel {
        private Boolean enabled = false;
        private String name;
        private String provider;
        private Integer dimensions;
        private String apiBaseUrl;
        private String apiKey;
        private String model;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        models.forEach(model -> modelMap.put(model.getName(), model));
    }
}
