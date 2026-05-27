package com.usoft.framework.bi.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import lombok.Data;

@Data
public class PromptProperties implements InitializingBean {

    private List<Prompt> prompts = new ArrayList<>();
    private Map<String, Prompt> promptMap = new HashMap<>();

    @Data
    public static class Prompt {
        private String name;
        private String model;
        private String template;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prompts.forEach(prompt -> promptMap.put(prompt.getName(), prompt));
    }
}
