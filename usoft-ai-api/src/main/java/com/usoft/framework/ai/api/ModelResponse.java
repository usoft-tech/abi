package com.usoft.framework.ai.api;

import java.util.Map;

import lombok.Data;

@Data
public class ModelResponse {
    private String id;
    private String name;
    private String provider;
    private String model;
    private String baseUrl;
    private Map<String, Object> extProps;
}
