package com.usoft.framework.common.sse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatSseEmitter<T> {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

    private static final MediaType CONTENT_TYPE = new MediaType("text", "event-stream", StandardCharsets.UTF_8);

    private static final String COMPLETE_EVENT = "complete";

    private static final String DEFAULT_EVENT = "message";

    private static final String HEARTBEAT_EVENT = "heartbeat";

    private static final String EMPTY = "";

    private static final long HEARTBEAT_INTERVAL = 5;

    private static final String DEFAULT_STATUS = "success";

    private ScheduledFuture<?> heartbeatTask;

    @Getter
    private SseEmitter emitter;

    @Getter
    private String requestId;

    private final List<Consumer<Throwable>> errorCallbacks = new ArrayList<>();
    private final List<Runnable> completionCallbacks = new ArrayList<>();
    private final List<Runnable> timeoutCallbacks = new ArrayList<>();

    private boolean completed = false;

    @Getter
    @Setter
    private String accessToken;

    private long lastMessageMillis;

    public HeartbeatSseEmitter() {
        init(null);
    }

    public HeartbeatSseEmitter(HttpServletRequest request) {
        this.requestId = request.getRequestId();
        this.accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (this.accessToken != null && this.accessToken.startsWith("Bearer ")) {
            this.accessToken = this.accessToken.substring(7);
        }
        init(null);
    }

    public HeartbeatSseEmitter(String requestId) {
        this.requestId = requestId;
        init(null);
    }

    public HeartbeatSseEmitter(Long timeout) {
        init(timeout);
    }

    public HeartbeatSseEmitter(String requestId, Long timeout) {
        this.requestId = requestId;
        init(timeout);
    }

    private void init(Long timeout) {
        this.lastMessageMillis = System.currentTimeMillis();
        this.emitter = new SseEmitter(timeout == null ? TimeUnit.HOURS.toMillis(10) : timeout) {
            @Override
            protected void extendResponse(ServerHttpResponse outputMessage) {
                HttpHeaders headers = outputMessage.getHeaders();
                if (headers.getContentType() == null) {
                    headers.setContentType(CONTENT_TYPE);
                }
            }
        };

        // 启动心跳调度
        this.heartbeatTask = executor.scheduleAtFixedRate(() -> {
            try {
                if (System.currentTimeMillis() - lastMessageMillis > emitter.getTimeout()) {
                    log.warn("HeartbeatSseEmitter.heartbeat timeout, requestId: {}", requestId);
                    this.completeWithError(new TimeoutException("sse timeout"));
                    return;
                }
                log.debug("HeartbeatSseEmitter.heartbeat, requestId: {}", requestId);
                this.send(HEARTBEAT_EVENT, null);
            } catch (Exception e) {
                log.error("HeartbeatSseEmitter.heartbeat error, requestId: {}", requestId, e);
                this.completeWithError(e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        this.emitter.onError(err -> errorCallbacks.forEach(callback -> callback.accept(err)));
        this.emitter.onTimeout(() -> timeoutCallbacks.forEach(Runnable::run));
        this.emitter.onCompletion(() -> {
            completionCallbacks.forEach(Runnable::run);
            log.info("HeartbeatSseEmitter.complete, requestId: {}", requestId);
        });

        log.debug("HeartbeatSseEmitter.init completed: callbacks registered & heartbeat scheduled, requestId: {}",
                requestId);
    }

    public synchronized void onError(Consumer<Throwable> callback) {
        errorCallbacks.add(callback);
    }

    public synchronized void onTimeout(Runnable callback) {
        timeoutCallbacks.add(callback);
    }

    public synchronized void onCompletion(Runnable callback) {
        completionCallbacks.add(callback);
    }

    public void send(String message) {
        this.send(DEFAULT_EVENT, message);
    }

    public void send(String event, String message) {
        this.send(new SseResponse<>(event, message));
    }

    public void send(String event, String id, String message) {
        this.send(event, id, DEFAULT_STATUS, message);
    }

    public void send(String event, String id, String status, String message) {
        this.send(event, id, status, message, null);
    }

    @SuppressWarnings("unchecked")
    public void send(String event, String id, String status, String message, Object data) {
        SseResponse<T> res = new SseResponse<>(event, message);
        res.setId(id);
        res.setStatus(status);
        res.setData((T) data);
        this.send(res);
    }

    public void send(SseResponse<T> object) {
        if (object == null || completed) {
            return;
        }
        object.setRequestId(requestId);
        if (object.getMessage() == null) {
            object.setMessage(EMPTY);
        }
        if (object.getEvent() == null) {
            object.setEvent(DEFAULT_EVENT);
        }
        try {
            // Use SseEventBuilder to set event name and id for frontend EventSource
            // listeners
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .id(java.util.UUID.randomUUID().toString())
                    .name(object.getEvent())
                    .data(object);
            emitter.send(builder);
            if (!StringUtils.equals(object.getEvent(), HEARTBEAT_EVENT)) {
                this.lastMessageMillis = System.currentTimeMillis();
            }
        } catch (Exception e) {
            if (StringUtils.equals(e.getMessage(), "ResponseBodyEmitter has already completed")) {
                this.completeWithError(e);
                return;
            }
            log.error("HeartbeatSseEmitter.send error, requestId: {}, event: {}, message: {}", requestId,
                    object.getEvent(), object.getMessage(), e);
            throw new RuntimeException("HeartbeatSseEmitter.send error, requestId: " + requestId);
        }
    }

    public final synchronized void complete() {
        try {
            heartbeatTask.cancel(true);
            SseResponse<T> completeResponse = new SseResponse<>(COMPLETE_EVENT, null);
            completeResponse.setFinished(true);
            this.send(completeResponse);
            emitter.complete();
            completed = true;
        } catch (Exception e) {
            this.completeWithError(e);
        }
    }

    public final synchronized void completeWithError(Throwable t) {
        completed = true;
        heartbeatTask.cancel(true);
        SseResponse<T> completeResponse = new SseResponse<>(COMPLETE_EVENT, null);
        completeResponse.setFinished(true);
        completeResponse.setStatus("error");
        completeResponse.setMessage(t.getMessage());
        this.send(completeResponse);
        emitter.completeWithError(t);
    }
}
