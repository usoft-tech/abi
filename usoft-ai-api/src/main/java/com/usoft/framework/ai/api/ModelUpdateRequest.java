package com.usoft.framework.ai.api;

import java.util.Map;

import lombok.Data;

@Data
public class ModelUpdateRequest {
    private String name;
    private String provider;
    private String model;
    private String baseUrl;
    private String apiKey;
    private Map<String, Object> extProps;
}
