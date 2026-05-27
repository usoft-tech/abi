package com.usoft.framework.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.usoft.framework.YamlPropertySourceFactory;
import com.usoft.framework.ai.store.DbChatMemoryStore;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.dashscope.DashScopeTextEmbedding;
import io.agentscope.core.embedding.openai.OpenAITextEmbedding;

@Configuration
@PropertySource(value = "classpath:s-model.yml", factory = YamlPropertySourceFactory.class)
public class ModelConfig {

    @Bean
    @ConfigurationProperties(prefix = "app")
    ModelProperties modelProperties() {
        return new ModelProperties();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(DbChatMemoryStore chatMemoryStore) {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore) // 设置存储对象
                        .build();
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.embedding-model", name = "enabled", havingValue = "true")
    EmbeddingModel embeddingModel(ModelProperties modelProperties) {
        com.usoft.framework.ai.config.ModelProperties.EmbeddingModel embeddingModel = modelProperties
                .getEmbeddingModel();
        switch (embeddingModel.getProvider()) {
            case "openai":
                return new OpenAITextEmbedding.Builder()
                        .apiKey(embeddingModel.getApiKey())
                        .baseUrl(embeddingModel.getApiBaseUrl())
                        .modelName(embeddingModel.getModel())
                        .dimensions(embeddingModel.getDimensions())
                        .build();
            case "dashscope":
                return new DashScopeTextEmbedding.Builder()
                        .apiKey(embeddingModel.getApiKey())
                        .modelName(embeddingModel.getModel())
                        .dimensions(embeddingModel.getDimensions())
                        .build();
            default:
                throw new IllegalArgumentException("不支持的嵌入模型提供程序: " + embeddingModel.getProvider());
        }
    }
}
