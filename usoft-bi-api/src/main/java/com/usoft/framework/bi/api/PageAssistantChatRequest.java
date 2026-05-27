package com.usoft.framework.bi.api;

import java.util.List;

import com.usoft.framework.bi.api.page.schema.PageSchema;
import com.usoft.framework.bi.api.page.schema.SchemaItem;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class PageAssistantChatRequest {
    private String bizType;
    private String bizId;
    private String conversationId;
    private String message;
    private List<File> files;
    private List<Agent> agents;
    private PageSchema page;
    private List<SchemaItem> pageItems;

    @Data
    @Accessors(chain = true)
    public static class File {
        private String id;
        private String name;
        private String extension;
    }

    @Data
    public static class Agent {
        private String id;
        private String name;
    }
}

