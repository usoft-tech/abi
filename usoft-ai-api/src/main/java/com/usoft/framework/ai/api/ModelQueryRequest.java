package com.usoft.framework.ai.api;

import com.usoft.framework.common.api.PageRequest;

public class ModelQueryRequest extends PageRequest {
    private String name;
    private String provider;
    private String keyword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
