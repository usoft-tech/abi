package com.usoft.framework.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.usoft.framework.utils.SpringUtils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.output.ServiceOutputParser;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天代理
 */
@Slf4j
public class ChatProxy {

    /**
     * 聊天
     * 
     * @param model   模型
     * @param message 消息
     * @return 回复
     */
    public static String doChat(String model, String message) {
        return doChat(model, null, message);
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param messages 消息
     * @return 回复
     */
    public static String doChatWithRetry(String model, String message) {
        return doRetry(() -> doChat(model, message));
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doChat(String model, String message, Class<T> responseType) {
        String response = doChat(model, toJsonSchemaMessage(message, responseType));
        return parseMessage(response, responseType);
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doChatWithRetry(String model, String message, Class<T> responseType) {
        return doRetry(() -> doChat(model, message, responseType));
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static String doChat(String model, String prompt, String message) {
        return tokenStream(getChatProxyService(model, prompt).streamChat(message));
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static String doChatWithRetry(String model, String prompt, String message) {
        return doRetry(() -> doChat(model, prompt, message));
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doChat(String model, String prompt, String message, Class<T> responseType) {
        String response = doChat(model, prompt, toJsonSchemaMessage(message, responseType));
        return parseMessage(response, responseType);
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doChatWithRetry(String model, String prompt, String message, Class<T> responseType) {
        return doRetry(() -> doChat(model, prompt, message, responseType));
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param messages 消息
     * @return 回复
     */
    public static String doChat(String model, List<ChatMessage> messages) {
        return doChat(model, null, messages);
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param messages 消息
     * @return 回复
     */
    public static String doChatWithRetry(String model, List<ChatMessage> messages) {
        return doRetry(() -> doChat(model, messages));
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param messages 消息
     * @return 回复
     */
    public static <T> T doChat(String model, List<ChatMessage> messages, Class<T> responseType) {
        return doChat(model, null, messages, responseType);
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param messages 消息
     * @return 回复
     */
    public static <T> T doChatWithRetry(String model, List<ChatMessage> messages, Class<T> responseType) {
        return doRetry(() -> doChat(model, messages, responseType));
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param prompt   提示
     * @param messages 消息
     * @return 回复
     */
    public static String doChat(String model, String prompt, List<ChatMessage> messages) {
        return tokenStream(getChatProxyService(model, prompt).streamChat(messages));
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param prompt   提示
     * @param messages 消息
     * @return 回复
     */
    public static String doChatWithRetry(String model, String prompt, List<ChatMessage> messages) {
        return doRetry(() -> doChat(model, prompt, messages));
    }

    /**
     * 聊天
     * 
     * @param model    模型
     * @param prompt   提示
     * @param messages 消息
     * @return 回复
     */
    public static <T> T doChat(String model, String prompt, List<ChatMessage> messages, Class<T> responseType) {
        String response = doChat(model, prompt, messages);
        return parseMessage(response, responseType);
    }

    /**
     * 聊天
     * 
     * @param model        模型
     * @param prompt       提示
     * @param messages     消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doChatWithRetry(String model, String prompt, List<ChatMessage> messages,
            Class<T> responseType) {
        return doRetry(() -> doChat(model, prompt, messages, responseType));
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param message  消息
     * @return 回复
     */
    public static String doMemoryChat(String memoryId, String model, String message) {
        return doMemoryChat(memoryId, model, null, message);
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param message  消息
     * @return 回复
     */
    public static String doMemoryChatWithRetry(String memoryId, String model, String message) {
        return doMemoryChatWithRetry(memoryId, model, null, message);
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param message  消息
     * @return 回复
     */
    public static <T> T doMemoryChat(String memoryId, String model, String message, Class<T> responseType) {
        return doMemoryChat(memoryId, model, null, message, responseType);
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param message  消息
     * @return 回复
     */
    public static <T> T doMemoryChatWithRetry(String memoryId, String model, String message, Class<T> responseType) {
        return doMemoryChatWithRetry(memoryId, model, null, message, responseType);
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param prompt   提示
     * @param message  消息
     * @return 回复
     */
    public static String doMemoryChat(String memoryId, String model, String prompt, String message) {
        return tokenStream(getMemoryChatProxyService(model, prompt).streamChat(memoryId, message));
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param prompt   提示
     * @param message  消息
     * @return 回复
     */
    public static String doMemoryChatWithRetry(String memoryId, String model, String prompt, String message) {
        return doRetry(() -> doMemoryChat(memoryId, model, prompt, message));
    }

    /**
     * 聊天
     * 
     * @param <T>          回复类型
     * @param memoryId     记忆ID
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> T doMemoryChat(String memoryId, String model, String prompt, String message,
            Class<T> responseType) {
        String response = doMemoryChat(memoryId, model, prompt, toJsonSchemaMessage(message, responseType));
        return parseMessage(response, responseType);
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param prompt   提示
     * @param message  消息
     * @return 回复
     */
    public static <T> T doMemoryChatWithRetry(String memoryId, String model, String prompt, String message,
            Class<T> responseType) {
        return doRetry(() -> doMemoryChat(memoryId, model, prompt, message, responseType));
    }

    /**
     * 应用模板
     * 
     * @param template  模板
     * @param variables 变量
     * @return 应用后的模板
     */
    public static String applyTemplate(String template, Map<String, Object> variables) {
        Prompt prompt = PromptTemplate.from(template).apply(variables);

        return prompt.text();
    }

    /**
     * 解析消息
     * 
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseMessage(String message, Class<T> responseType) {
        try {
            ServiceOutputParser serviceOutputParser = new ServiceOutputParser();
            return (T) serviceOutputParser.parseText(responseType, message);
        } catch (Exception e) {
            return JSON.parseObject(message, responseType);
        }
    }

    /**
     * 转换为 JSON 模式消息
     * 
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    public static <T> String toJsonSchemaMessage(String message, Class<T> responseType) {
        ServiceOutputParser serviceOutputParser = new ServiceOutputParser();
        return message + "\n\n" + serviceOutputParser.outputFormatInstructions(responseType);
    }

    /**
     * 重试
     * 
     * @param supplier 供应商
     * @return 回复
     */
    private static <T> T doRetry(Supplier<T> supplier) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.error("doRetry failed, retryCount: {}", retryCount, e);
                retryCount++;
            }
        }
        throw new RuntimeException("doRetry failed after " + maxRetries + " retries");
    }

    /**
     * tokenStream 转换为字符串
     * 
     * @param tokenStream 令牌流
     * @return 回复
     */
    private static String tokenStream(TokenStream tokenStream) {
        StringBuilder sb = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);
        tokenStream
                .onPartialResponse(sb::append)
                .onCompleteResponse(res -> latch.countDown())
                .onError(error -> {
                    latch.countDown();
                    log.error("Chat error: {}", error.getMessage(), error);
                    throw new RuntimeException("Chat error", error);
                })
                .start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return sb.toString();
    }

    /**
     * 获取聊天代理服务
     * 
     * @param model  模型
     * @param prompt 提示
     * @return 聊天代理服务
     */
    private static ChatProxyService getChatProxyService(String model, String prompt) {
        ChatModelManager chatModelManager = SpringUtils.getBean(ChatModelManager.class);
        StreamingChatModel streamingChatModel = chatModelManager.getStreamingChatModel(model);
        if (streamingChatModel == null) {
            throw new IllegalArgumentException("Chat model not found: " + model);
        }
        AiServices<ChatProxyService> builder = AiServices.builder(ChatProxyService.class)
                .streamingChatModel(streamingChatModel);
        if (StringUtils.isNotBlank(prompt)) {
            builder.systemMessageProvider(x -> prompt);
        }
        return builder.build();
    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param model    模型
     * @param prompt   提示
     * @param message  消息
     * @return 回复
     */
    private static ChatProxyServiceWithMemory getMemoryChatProxyService(String model, String prompt) {
        ChatMemoryProvider chatMemoryProvider = SpringUtils.getBean(ChatMemoryProvider.class);
        ChatModelManager chatModelManager = SpringUtils.getBean(ChatModelManager.class);
        StreamingChatModel streamingChatModel = chatModelManager.getStreamingChatModel(model);
        if (streamingChatModel == null) {
            throw new IllegalArgumentException("Chat model not found: " + model);
        }
        AiServices<ChatProxyServiceWithMemory> builder = AiServices.builder(ChatProxyServiceWithMemory.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider);
        if (StringUtils.isNotBlank(prompt)) {
            builder.systemMessageProvider(x -> prompt);
        }
        return builder.build();
    }

    interface ChatProxyService {

        String chat(List<ChatMessage> messages);

        TokenStream streamChat(List<ChatMessage> messages);

        String chat(String message);

        TokenStream streamChat(String message);
    }

    interface ChatProxyServiceWithMemory {

        String chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String message);

        TokenStream streamChat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String message);
    }
}
