package com.usoft.framework.ai;

import com.usoft.framework.ai.api.ModelResponse;
import com.usoft.framework.ai.service.ModelService;
import com.usoft.framework.utils.SpringUtils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatApp {

    private String name;
    private String description;
    private String modelId;
    private Boolean enable;
    private String template;

    private ModelResponse model;

    public ModelResponse getModel() {
        if (model == null) {
            model = SpringUtils.getBean(ModelService.class).get(modelId);
        }
        return model;
    }

    public boolean enabled() {
        return enable != null && enable;
    }
}
