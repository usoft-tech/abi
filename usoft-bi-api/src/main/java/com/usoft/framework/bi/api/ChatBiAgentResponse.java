package com.usoft.framework.bi.api;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 智能体响应
 */
@Data
@Builder
public class ChatBiAgentResponse {

    private String id;
    private String name;
    private Boolean group;
    private String type;

    private List<ChatBiAgentResponse> children;
}
