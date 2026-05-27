package com.usoft.framework.bi.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.usoft.framework.bi.api.page.schema.PageSchema;
import com.usoft.framework.bi.api.page.schema.SchemaItem;

import lombok.Builder;
import lombok.Data;

@Data
public class PageAssistantChatResponse {
    private String id;
    private Answer answer;
    private Instant createdAt;
    private Conversation conversation;
    private String question;

    @Data
    public static class Answer {
        private String answer;
        private List<Plan> plans;
        private Effect effect;
        private List<Extra> extra = new ArrayList<>();
    }

    @Data
    public static class Effect {
        private List<Object> datasets;
        private PageSchema schema;
        private List<EffectSchemaItem> pageItems;
    }

    public enum Position {
        before,
        after,
        replace,
        insert;
    }

    @Data
    public static class EffectSchemaItem {
        private String targetId;
        private Position position;
        private String insertSlot;
        private SchemaItem pageItem;
    }

    @Data
    @Builder
    public static class Extra {
        private String id;
        private String element;
        private String action;
        private String area;
    }

    @Data
    @Builder
    public static class Conversation {
        private String id;
        private String name;
        private Instant createdAt;
        private Boolean isActived;
    }

    @Data
    public static class Plans {
        private List<Plan> elements;
    }

    @Data
    public static class Plan {
        private String name;
        private String description;
    }
}
