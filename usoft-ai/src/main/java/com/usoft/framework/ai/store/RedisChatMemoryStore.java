package com.usoft.framework.ai.store;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;

/**
 * Redis 聊天内存存储
 */
@Repository
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {
    private static final String CHAT_MEMORY_KEY_PREFIX = "chat_memory:";

    // 注入redisTemplate
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 获取会话消息
        String json = redisTemplate.opsForValue().get(redisKey(memoryId));
        // 把json数据转成List<ChatMessage>
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 更新会话消息
        // 1.把list转成json数据
        String json = ChatMessageSerializer.messagesToJson(list);

        // 2.保存到redis中
        redisTemplate.opsForValue().set(redisKey(memoryId), json, Duration.ofDays(1));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(redisKey(memoryId));
    }

    private String redisKey(Object memoryId) {
        return CHAT_MEMORY_KEY_PREFIX + memoryId;
    }

}
