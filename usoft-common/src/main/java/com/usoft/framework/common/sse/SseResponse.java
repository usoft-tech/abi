package com.usoft.framework.common.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SseResponse<T> {
    private static final String DEFAULT_STATUS = "success";

    private String id;
    private String event;
    private String status;
    private String message;
    private T data;
    protected boolean finished;
    private String requestId;
    private Long timestamp;
    private Long duration;

    public SseResponse(String event, String message) {
        this.id = java.util.UUID.randomUUID().toString();
        this.event = event;
        this.status = DEFAULT_STATUS;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public SseResponse(String event, T data) {
        this.id = java.util.UUID.randomUUID().toString();
        this.event = event;
        this.data = data;
        this.status = DEFAULT_STATUS;
        this.timestamp = System.currentTimeMillis();
    }
}
