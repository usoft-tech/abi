package com.usoft.framework.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.usoft.framework.ai.config.ModelProperties;
import com.usoft.framework.ai.config.ModelProperties.Model;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;

/**
 * 聊天模型管理器
 */
@Service
@RequiredArgsConstructor
public class ChatModelManager {
    private final ModelProperties properties;

    private final Map<String, ChatModel> modelMap = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatModel> streamingModelMap = new ConcurrentHashMap<>();

    /**
     * 获取聊天模型
     * 
     * @param name 模型名称
     * @return 聊天模型
     */
    public ChatModel getChatModel(String name) {
        return modelMap.computeIfAbsent(name, this::createChatModel);
    }

    /**
     * 获取流式聊天模型
     * 
     * @param name 模型名称
     * @return 流式聊天模型
     */
    public StreamingChatModel getStreamingChatModel(String name) {
        return streamingModelMap.computeIfAbsent(name, this::createStreamingChatModel);
    }

    private ChatModel createChatModel(String name) {
        Model e = properties.getModelMap().get(name);
        if (e == null) {
            return null;
        }
        String provider = e.getProvider() == null ? "openai" : e.getProvider().toLowerCase();
        switch (provider) {
            case "openai":
                return OpenAiChatModel.builder()
                        .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "https://api.openai.com/v1"))
                        .apiKey(e.getApiKey())
                        .modelName(e.getModel())
                        .temperature(e.getTemperature())
                        .maxTokens(e.getMaxTokens())
                        .returnThinking(false)
                        .build();
            case "ollama":
                return OllamaChatModel.builder()
                        .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "http://localhost:11434"))
                        .modelName(e.getModel())
                        .temperature(e.getTemperature())
                        .build();
            default:
                break;
        }
        return null;
    }

    private StreamingChatModel createStreamingChatModel(String name) {
        Model e = properties.getModelMap().get(name);
        if (e == null) {
            return null;
        }
        String provider = e.getProvider() == null ? "" : e.getProvider().toLowerCase();
        switch (provider) {
            case "openai":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "https://api.openai.com/v1"))
                        .apiKey(e.getApiKey())
                        .modelName(e.getModel())
                        .temperature(e.getTemperature())
                        .maxTokens(e.getMaxTokens())
                        .returnThinking(false)
                        .build();
            case "ollama":
                return OllamaStreamingChatModel.builder()
                        .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "http://localhost:11434"))
                        .modelName(e.getModel())
                        .temperature(e.getTemperature())
                        .build();
            default:
                break;
        }
        return null;
    }
}
