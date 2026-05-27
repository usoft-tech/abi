package com.usoft.framework.ai;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.usoft.framework.ai.config.ModelProperties;
import com.usoft.framework.ai.session.JdbcSessionFactory;
import com.usoft.framework.common.sse.HeartbeatSseEmitter;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.utils.SpringUtils;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.ReasoningChunkEvent;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OllamaChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.ollama.OllamaOptions;
import io.agentscope.core.pipeline.SequentialPipeline;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.RAGMode;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.rag.reader.PDFReader;
import io.agentscope.core.rag.reader.ReaderInput;
import io.agentscope.core.rag.reader.SplitStrategy;
import io.agentscope.core.rag.store.InMemoryStore;
import io.agentscope.core.session.SessionManager;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.multimodal.OpenAIMultiModalTool;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AgentScopeProxy {
    private static final ExecutionConfig MODEL_CONFIG = ExecutionConfig.builder()
            .timeout(Duration.ofMinutes(25))
            .maxAttempts(3)
            .build();

    private static final ExecutionConfig TOOL_CONFIG = ExecutionConfig.builder()
            .timeout(Duration.ofSeconds(30))
            // 工具通常不重试
            .maxAttempts(1)
            .build();

    private final SkillBox skillBox;
    @Getter
    private final AgentSkillRepository skillRepository;
    private final DataSource dataSource;

    private static final AutoContextConfig AUTO_CONTEXT_CONFIG = AutoContextConfig.builder()
            .msgThreshold(30)
            .lastKeep(10)
            .tokenRatio(0.3)
            .build();

    private final ModelProperties properties;

    private final Map<String, LocalChatModel> modelMap = new ConcurrentHashMap<>();

    public AgentScopeProxy(Environment env, DataSource dataSource, ModelProperties properties) {
        this.properties = properties;
        String skillStoreDir = env.getProperty("app.ai.skill-store-dir");
        this.skillRepository = new FileSystemSkillRepository(Path.of(skillStoreDir));
        this.dataSource = dataSource;
        this.skillBox = new SkillBox();
        this.skillBox.setAutoUploadSkill(false);
        this.skillRepository.getAllSkills().forEach(this.skillBox::registerSkill);

    }

    /**
     * 聊天
     * 
     * @param memoryId 记忆ID
     * @param agentName 智能体名称
     * @param model    模型
     * @param prompt   提示
     * @param message  消息
     * @return 回复
     */
    public ResultWithId<String> doMemoryChat(String memoryId, String agentName, String model, String prompt, String message) {
        return this.doMemoryChat(memoryId, agentName, model, prompt, message, String.class);
    }

    /**
     * 聊天
     * 
     * @param <T>          回复类型
     * @param memoryId     记忆ID
     * @param agentName    智能体名称
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    @SuppressWarnings({ "hiding" })
    public <T> ResultWithId<T> doMemoryChat(String memoryId, String agentName, String model, String prompt, String message,
            Class<T> responseType) {

        Msg msg = Msg.builder().textContent(message).build();

        return doMemoryChat(memoryId, agentName, model, prompt, msg, responseType);
    }

    /**
     * 聊天
     * 
     * @param <T>          回复类型
     * @param memoryId     记忆ID
     * @param agentName    智能体名称
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param responseType 回复类型
     * @return 回复
     */
    @SuppressWarnings({ "hiding" })
    public <T> ResultWithId<T> doMemoryChat(String memoryId, String agentName, String model, String prompt, Msg message,
            Class<T> responseType) {
        return doMemoryChat(memoryId, agentName, model, prompt, message, null, responseType);
    }

    /**
     * 聊天
     * 
     * @param <T>          回复类型
     * @param memoryId     记忆ID
     * @param agentName    智能体名称
     * @param model        模型
     * @param prompt       提示
     * @param message      消息
     * @param knowledge    知识库
     * @param responseType 回复类型
     * @return 回复
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public <T> ResultWithId<T> doMemoryChat(String memoryId, String agentName, String model, String prompt, Msg message,
            Knowledge knowledge,
            Class<T> responseType) {
        LocalChatModel chatModel = getChatModel(model);
        if (chatModel == null) {
            throw new IllegalArgumentException("Chat model not found: " + model);
        }
        // 创建内存
        AutoContextMemory memory = new AutoContextMemory(AUTO_CONTEXT_CONFIG, chatModel.getModel());

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory));
        // toolkit.registerTool(new PdfTool());
        toolkit.registerTool(
                new OpenAIMultiModalTool(chatModel.getConfig().getApiKey(), chatModel.getConfig().getApiBaseUrl()));
        // toolkit.registerTool(new OpenAIMultiModalTool(chatModel.getApiKey()));

        ReActAgent actAgent = ReActAgent.builder()
                .name(chatModel.getName())
                .model(chatModel.getModel())
                .sysPrompt(prompt)
                .memory(memory)
                // .hook(hook)
                .skillBox(skillBox)
                .knowledge(knowledge)
                .ragMode(knowledge != null ? RAGMode.AGENTIC : null)
                .retrieveConfig(
                        knowledge != null ? RetrieveConfig.builder()
                                .limit(3)
                                .scoreThreshold(0.5)
                                .build() : null)
                .toolkit(toolkit)
                .enableMetaTool(true)
                .modelExecutionConfig(MODEL_CONFIG)
                .toolExecutionConfig(TOOL_CONFIG)
                .build();

        SessionManager sessionManager = SessionManager.forSessionId(memoryId)
                .withSession(JdbcSessionFactory.createSession(dataSource))
                .addComponent(actAgent)
                .addComponent(memory);

        SequentialPipeline pipeline = SequentialPipeline.builder()
                .addAgent(actAgent)
                .build();

        boolean isStringResponse = responseType == null || responseType == String.class;

        sessionManager.loadIfExists();
        Msg response = pipeline.execute(message).block();
        String textContent = response.getTextContent();
        if (StringUtils.isBlank(textContent)) {
            return null;
        }
        T t = null;
        if (!isStringResponse) {
            t = ObjectMapperUtils.fromJson(textContent, responseType);
        } else {
            t = (T) textContent;
        }
        sessionManager.saveSession();
        return new ResultWithId<T>().setId(response.getId()).setResult(t);
    }

    /**
     * 聊天
     * 
     * @param memoryId  记忆ID
     * @param agentName 智能体名称
     * @param model     模型
     * @param prompt    提示
     * @param message   消息
     * @param knowledge 知识库
     * @param emitter   SSE发射器
     */
    public String doMemoryChat(String memoryId, String agentName, String model, String prompt, Msg message,
            Knowledge knowledge,
            HeartbeatSseEmitter<?> emitter) {
        LocalChatModel chatModel = getChatModel(model);
        if (chatModel == null) {
            throw new IllegalArgumentException("Chat model not found: " + model);
        }
        // 创建内存
        AutoContextMemory memory = new AutoContextMemory(AUTO_CONTEXT_CONFIG, chatModel.getModel());

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory));
        // toolkit.registerTool(new PdfTool());
        toolkit.registerTool(
                new OpenAIMultiModalTool(chatModel.getConfig().getApiKey(), chatModel.getConfig().getApiBaseUrl()));
        // toolkit.registerTool(new OpenAIMultiModalTool(chatModel.getApiKey()));

        ReActAgent actAgent = ReActAgent.builder()
                .name(agentName)
                .model(chatModel.getModel())
                .sysPrompt(prompt)
                .memory(memory)
                .hook(new EmitterHook(emitter))
                .skillBox(skillBox)
                .knowledge(knowledge)
                .ragMode(knowledge != null ? RAGMode.AGENTIC : null)
                .retrieveConfig(
                        knowledge != null ? RetrieveConfig.builder()
                                .limit(3)
                                .scoreThreshold(0.5)
                                .build() : null)
                .toolkit(toolkit)
                .enableMetaTool(true)
                .modelExecutionConfig(MODEL_CONFIG)
                .toolExecutionConfig(TOOL_CONFIG)
                .build();

        SessionManager sessionManager = SessionManager.forSessionId(memoryId)
                .withSession(JdbcSessionFactory.createSession(dataSource))
                .addComponent(actAgent)
                .addComponent(memory);

        SequentialPipeline pipeline = SequentialPipeline.builder()
                .addAgent(actAgent)
                .build();

        sessionManager.loadIfExists();
        Msg response = pipeline.execute(message).block();
        emitter.complete();
        sessionManager.saveSession();
        return response.getTextContent();
    }

    
    /**
     * 聊天
     * 
     * @param agentName 智能体名称
     * @param model     模型
     * @param prompt    提示
     * @param message   消息
     * @param responseType 响应类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    public <T> T doChat(String agentName, String model, String prompt, Msg message, Class<T> responseType) {
        LocalChatModel chatModel = getChatModel(model);
        if (chatModel == null) {
            throw new IllegalArgumentException("Chat model not found: " + model);
        }
        // 创建内存
        AutoContextMemory memory = new AutoContextMemory(AUTO_CONTEXT_CONFIG, chatModel.getModel());

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory));
        // toolkit.registerTool(new PdfTool());
        toolkit.registerTool(
                new OpenAIMultiModalTool(chatModel.getConfig().getApiKey(), chatModel.getConfig().getApiBaseUrl()));
        // toolkit.registerTool(new OpenAIMultiModalTool(chatModel.getApiKey()));

        ReActAgent actAgent = ReActAgent.builder()
                .name(agentName)
                .model(chatModel.getModel())
                .sysPrompt(prompt)
                .memory(memory)
                // .hook(hook)
                .skillBox(skillBox)
                .toolkit(toolkit)
                .enableMetaTool(true)
                .modelExecutionConfig(MODEL_CONFIG)
                .toolExecutionConfig(TOOL_CONFIG)
                .build();


        SequentialPipeline pipeline = SequentialPipeline.builder()
                .addAgent(actAgent)
                .build();

        boolean isStringResponse = responseType == null || responseType == String.class;

        Msg response = pipeline.execute(message).block();
        String textContent = response.getTextContent();
        if (StringUtils.isBlank(textContent)) {
            return null;
        }
        T t = null;
        if (!isStringResponse) {
            t = ObjectMapperUtils.fromJson(textContent, responseType);
        } else {
            t = (T) textContent;
        }
        return t;
    }

    public boolean deleteMessage(String memoryId, String messageId) {

        AutoContextMemory memory = new AutoContextMemory(AUTO_CONTEXT_CONFIG, null);
        SessionManager sessionManager = SessionManager.forSessionId(memoryId)
                .withSession(JdbcSessionFactory.createSession(dataSource))
                .addComponent(memory);
        sessionManager.loadIfExists();
        List<Msg> messages = memory.getMessages();
        messages.stream().filter(m -> m.getId().equals(messageId)).findFirst().ifPresent(m -> {
            memory.deleteMessage(messages.indexOf(m));
        });
        sessionManager.saveSession();
        return true;
    }

    /**
     * 获取聊天模型
     * 
     * @param name 模型名称
     * @return 聊天模型
     */
    private LocalChatModel getChatModel(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return modelMap.computeIfAbsent(name, this::createChatModel);
    }

    private LocalChatModel createChatModel(String name) {
        com.usoft.framework.ai.config.ModelProperties.Model e = properties.getModelMap().get(name);
        if (e == null) {
            return null;
        }
        String provider = e.getProvider() == null ? "openai" : e.getProvider().toLowerCase();
        GenerateOptions generateOptions = GenerateOptions.builder()
                .temperature(e.getTemperature())
                .maxTokens(e.getMaxTokens())
                .stream(true)
                .build();
        switch (provider) {
            case "dashscope":
                return LocalChatModel.builder()
                        .name(name)
                        .config(e)
                        .model(DashScopeChatModel.builder()
                                // .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "https://dashscope.aliyuncs.com/api/v1"))
                                .apiKey(e.getApiKey())
                                .modelName(e.getModel())
                                .enableThinking(true)
                                .defaultOptions(generateOptions)
                                .formatter(new DashScopeChatFormatter())
                                .build())
                        .build();
            case "openai":
                return LocalChatModel.builder()
                        .name(name)
                        .config(e)
                        .model(OpenAIChatModel.builder()
                                .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "https://api.openai.com/v1"))
                                .apiKey(e.getApiKey())
                                .modelName(e.getModel())
                                .generateOptions(generateOptions)
                                .build())
                        .build();
            case "ollama":
                return LocalChatModel.builder()
                        .name(name)
                        .config(e)
                        .model(OllamaChatModel.builder()
                                .baseUrl(StringUtils.defaultIfBlank(e.getApiBaseUrl(), "http://localhost:11434"))
                                .modelName(e.getModel())
                                .defaultOptions(OllamaOptions.fromGenerateOptions(generateOptions))
                                .build())
                        .build();
            default:
                break;
        }
        return null;
    }

    @Data
    @Builder
    private static class LocalChatModel {
        private String name;
        private ModelProperties.Model config;
        private Model model;
    }

    public static class PdfTool {
        @Tool(name = "read_pdf", description = "Read content from PDF files and retrieve relevant information based on a search query. This tool is useful for extracting specific details or answering questions from provided PDF documents.")
        public Mono<ToolResultBlock> readPdf(
                @ToolParam(name = "query", description = "The search query or question used to retrieve relevant context from the PDF files.") String query,
                @ToolParam(name = "pdf_paths", description = "A comma-separated list of absolute paths to the PDF files to be processed (e.g., 'd:/docs/file1.pdf,d:/docs/file2.pdf').") String pdfPaths) {
            if (StringUtils.isBlank(pdfPaths)) {
                return Mono.empty();
            }
            // 1. 创建知识库
            EmbeddingModel embeddingModel = SpringUtils.getBean(EmbeddingModel.class);
            if (embeddingModel == null) {
                return Mono.empty();
            }

            Knowledge knowledge = SimpleKnowledge.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(InMemoryStore.builder().dimensions(1024).build())
                    .build();
            // PDF
            Arrays.stream(pdfPaths.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .forEach(file -> {
                        PDFReader pdfReader = new PDFReader(512, SplitStrategy.SEMANTIC, 50);
                        List<Document> blocks = pdfReader
                                .read(ReaderInput.fromPath(Paths.get(file))).block();
                        knowledge.addDocuments(blocks).block();
                    });
            List<Document> retrieves = knowledge
                    .retrieve(query, RetrieveConfig.builder().limit(5).scoreThreshold(0.5).build()).block();
            List<ContentBlock> blocks = retrieves.stream().map(res -> res.getMetadata().getContent()).toList();
            if (blocks.isEmpty()) {
                return Mono.empty();
            }
            return Mono.just(ToolResultBlock.builder().output(blocks).build());
        }
    }

    private static class EmitterHook implements Hook {
        private final HeartbeatSseEmitter<?> emitter;

        public EmitterHook(HeartbeatSseEmitter<?> emitter) {
            this.emitter = emitter;
        }

        @Override
        public <T extends HookEvent> Mono<T> onEvent(T event) {
            if (event instanceof ReasoningChunkEvent reasoningChunkEvent) {
                reasoningChunkEvent.getIncrementalChunk().getContent()
                        .forEach(block -> {
                            switch (block) {
                                case ThinkingBlock thinkBlock:
                                    emitter.send("thinking", thinkBlock.getThinking());
                                    break;
                                case TextBlock textBlock:
                                    emitter.send("text", textBlock.getText());
                                    break;
                                case ImageBlock imageBlock:
                                    emitter.send("image", imageBlock.getSource().toString());
                                    break;
                                case ToolUseBlock toolUseBlock:
                                    emitter.send("toolUse", toolUseBlock.getContent());
                                    break;
                                default:
                                    break;
                            }
                        });
            }
            return Mono.just(event);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ResultWithId<T> {
        private String id;
        private T result;
    }
}