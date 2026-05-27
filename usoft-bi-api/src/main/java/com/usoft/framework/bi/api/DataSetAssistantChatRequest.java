package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.page.schema.SchemaItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DataSetAssistantChatRequest {

    @NotBlank(message = "数据库不能为空")
    private String dbId;
    @NotBlank(message = "数据主题不能为空")
    private String title;
    @NotBlank(message = "数据描述不能为空")
    private String description;
    @NotNull(message = "目标页面项不能为空")
    private SchemaItem schemaItem;
}
