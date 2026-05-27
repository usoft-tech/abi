package com.usoft.framework.bi.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.ai.AgentScopeProxy;
import com.usoft.framework.ai.AgentScopeProxy.ResultWithId;
import com.usoft.framework.ai.api.ChatMessageAgent;
import com.usoft.framework.ai.api.ChatMessageFile;
import com.usoft.framework.ai.api.ConversationMessageCreateRequest;
import com.usoft.framework.ai.api.ConversationMessageResponse;
import com.usoft.framework.ai.api.ConversationResponse;
import com.usoft.framework.ai.service.ConversationMessageService;
import com.usoft.framework.ai.service.ConversationService;
import com.usoft.framework.bi.api.PageAssistantChatRequest;
import com.usoft.framework.bi.api.PageAssistantChatRequest.Agent;
import com.usoft.framework.bi.api.PageAssistantChatRequest.File;
import com.usoft.framework.bi.api.PageAssistantChatResponse;
import com.usoft.framework.bi.api.PageAssistantChatResponse.Answer;
import com.usoft.framework.bi.api.PageAssistantChatResponse.Conversation;
import com.usoft.framework.bi.api.PageAssistantChatResponse.Effect;
import com.usoft.framework.bi.api.PageAssistantChatResponse.EffectSchemaItem;
import com.usoft.framework.bi.api.PageAssistantChatResponse.Extra;
import com.usoft.framework.bi.api.PageAssistantChatResponse.Plan;
import com.usoft.framework.bi.api.PageCreateRequest;
import com.usoft.framework.bi.api.PageQueryRequest;
import com.usoft.framework.bi.api.PageReportCreateRequest;
import com.usoft.framework.bi.api.PageResponse;
import com.usoft.framework.bi.api.PageUpdateRequest;
import com.usoft.framework.bi.api.page.schema.PageInterpretation;
import com.usoft.framework.bi.api.page.schema.PageSchema;
import com.usoft.framework.bi.api.page.schema.SchemaItem;
import com.usoft.framework.bi.api.page.schema.SchemaItemDatasource;
import com.usoft.framework.bi.config.PromptProperties;
import com.usoft.framework.bi.config.PromptProperties.Prompt;
import com.usoft.framework.bi.entity.DatasourceDbEntity;
import com.usoft.framework.bi.entity.DatasourceFieldEntity;
import com.usoft.framework.bi.entity.DatasourceTableEntity;
import com.usoft.framework.bi.entity.PageEntity;
import com.usoft.framework.bi.mapper.PageMapper;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.sse.HeartbeatSseEmitter;
import com.usoft.framework.common.utils.IdUtils;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.SysFileResponse;
import com.usoft.framework.system.api.SysIconQueryRequest;
import com.usoft.framework.system.api.SysIconResponse;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.config.UploadProperties;
import com.usoft.framework.system.service.SysFileService;
import com.usoft.framework.system.service.SysIconService;
import com.usoft.framework.system.service.TenantService;
import com.usoft.framework.utils.ExcelUtils;
import com.usoft.framework.utils.ExcelUtils.CachedExcelDatabase;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.utils.SpringUtils;

import dev.langchain4j.model.output.structured.Description;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.formatter.MediaUtils;
import io.agentscope.core.message.AudioBlock;
import io.agentscope.core.message.Base64Source;
import io.agentscope.core.message.ImageBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.Msg.Builder;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.knowledge.SimpleKnowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.reader.PDFReader;
import io.agentscope.core.rag.reader.ReaderInput;
import io.agentscope.core.rag.reader.SplitStrategy;
import io.agentscope.core.rag.store.InMemoryStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PageService {
    private final PageMapper pageMapper;

    private final ConversationService conversationService;

    private final ConversationMessageService conversationMessageService;

    private final DataSourceService datasourceService;

    private final SysFileService sysFileService;

    private final SysIconService sysIconService;

    private final TenantService tenantService;

    private final PageReportService reportService;

    @Value("${app.bi.chat-file-db-path}")
    private String chatFileDbPath;

    private final AgentScopeProxy agentScopeProxy;

    // 创建页面架构提示词
    private final Prompt createPageSchemaByAgentPrompt;
    // 迭代页面架构提示词
    private final Prompt iterationPageSchemaByAgentPrompt;
    // 迭代页面项提示词
    private final Prompt iterationPageItemByAgentPrompt;

    private final Prompt generateReportPrompt;

    private final UploadProperties uploadProperties;

    public PageService(PageMapper pageMapper,
            ConversationService conversationService,
            ConversationMessageService conversationMessageService,
            DataSourceService datasourceService,
            SysFileService sysFileService,
            SysIconService sysIconService,
            TenantService tenantService,
            PageReportService reportService,
            PromptProperties promptProperties,
            AgentScopeProxy agentScopeProxy,
            UploadProperties uploadProperties) {
        this.pageMapper = pageMapper;
        this.conversationService = conversationService;
        this.conversationMessageService = conversationMessageService;
        this.datasourceService = datasourceService;
        this.sysFileService = sysFileService;
        this.sysIconService = sysIconService;
        this.tenantService = tenantService;
        this.reportService = reportService;
        this.agentScopeProxy = agentScopeProxy;

        this.uploadProperties = uploadProperties;

        // 创建页面架构提示词
        this.createPageSchemaByAgentPrompt = promptProperties.getPromptMap().get("create-page-schema");
        // 迭代页面架构提示词
        this.iterationPageSchemaByAgentPrompt = promptProperties.getPromptMap().get("iteration-page-schema");
        // 迭代页面项提示词
        this.iterationPageItemByAgentPrompt = promptProperties.getPromptMap().get("iteration-page-item");

        // 生成报表提示词
        this.generateReportPrompt = promptProperties.getPromptMap().get("generate-report-content");
    }

    /**
     * 创建页面
     */
    public PageResponse create(PageCreateRequest req) {
        PageEntity e = new PageEntity();
        BeanMapper.mapper(req, e);
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        pageMapper.insert(e);
        return toResponse(e);
    }

    /**
     * 更新页面
     */
    public PageResponse update(String id, PageUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        PageEntity e = pageMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        BeanMapper.mapper(req, e);

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        pageMapper.update(e);
        return toResponse(e);
    }

    /**
     * 更新页面架构
     */
    public PageResponse updateSchema(String id, PageSchema req) {
        String tenantId = TenantContext.getTenantId();
        PageEntity e = pageMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        BeanMapper.mapper(req, e);
        e.setSchemaJson(ObjectMapperUtils.toJson(req));

        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        pageMapper.update(e);
        return toResponse(e);
    }

    /**
     * 软删除页面
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        PageEntity e = pageMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        tenantService.checkTenantUserAuthKey(e.getTenantId(), UserHolder.userId(), TenantAuthKey.ADMIN);
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return pageMapper.update(e) > 0;
    }

    /**
     * 获取页面详情
     */
    public PageResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        PageEntity e = pageMapper.selectOneById(id);
        if (e == null || (tenantId != null && !tenantId.equals(e.getTenantId()))) {
            return null;
        }
        PageResponse r = toResponse(e);
        if (CollectionUtils.isNotEmpty(r.getExcelIds())) {
            r.setExcels(
                    sysFileService.listByIds(r.getExcelIds()).stream().map(this::toFile).collect(Collectors.toList()));
        }
        return r;
    }

    private com.usoft.framework.bi.api.PageResponse.File toFile(SysFileResponse e) {
        com.usoft.framework.bi.api.PageResponse.File r = new com.usoft.framework.bi.api.PageResponse.File();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setExtension(e.getName().substring(e.getName().lastIndexOf(".")));
        return r;
    }

    /**
     * 分页查询页面
     */
    public com.usoft.framework.common.api.PageResponse<PageResponse> list(PageQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .select("id", "name", "description", "cover", "status", "industry")
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getStatus() != null) {
            qw.and("status = ?", req.getStatus());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ? OR description LIKE ?)", kw, kw);
        }
        qw.orderBy("created_at", false);
        return PageHelper.apply(pageMapper, qw, req, this::toResponse);
    }

    /**
     * 转换为页面响应
     */
    private PageResponse toResponse(PageEntity e) {
        PageResponse r = new PageResponse();
        BeanMapper.mapper(e, r);
        r.setSchema(ObjectMapperUtils.fromJson(e.getSchemaJson(), PageSchema.class));
        r.setDbIds(e.getDbIds() != null && !e.getDbIds().isBlank() ? Arrays.asList(e.getDbIds().split(","))
                : new ArrayList<>());
        r.setExcelIds(e.getExcelIds() != null && !e.getExcelIds().isBlank() ? Arrays.asList(e.getExcelIds().split(","))
                : new ArrayList<>());
        return r;
    }

    /**
     * 页面助手聊天
     */
    @Transactional
    public PageAssistantChatResponse assistantChat(PageAssistantChatRequest req) {
        ConversationResponse conversation;
        if (StringUtils.isBlank(req.getConversationId())) {
            conversation = conversationService.create(req.getBizType(), req.getBizId(), req.getMessage());
            req.setConversationId(conversation.getId());

            conversationService.active(conversation);
        } else {
            conversation = conversationService.get(req.getConversationId());
        }

        // PageResponse page = get(req.getBizId());

        PageAssistantChatResponse response = new PageAssistantChatResponse();

        response.setQuestion(req.getMessage());
        response.setConversation(Conversation.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .isActived(true)
                .build());

        PageSchema schema = req.getPage();
        if (schema == null || CollectionUtils.isEmpty(schema.getItems())) {
            createPageSchemaByAgent(req, conversation, response);
        } else if (!CollectionUtils.isEmpty(req.getPageItems())) {
            iterationPageItemByAgent(req, conversation, response);
        } else {
            iterationPageSchemaByAgent(req, conversation, response);
        }

        return response;
    }

    /**
     * 页面设计解释
     */
    public void interpret(PageInterpretation interpretation, HeartbeatSseEmitter<?> emitter, String tenantId,
            String username) {
        String prompt = new String(generateReportPrompt.getTemplate());
        prompt = prompt.replace("{{page_schema}}", ObjectMapperUtils.toJson(interpretation));

        String reportId = IdUtils.randomIfAbsent(interpretation.getReportId());

        Msg msg = Msg.builder().build();
        String model = generateReportPrompt.getModel();
        String response = agentScopeProxy.doMemoryChat(reportId, generateReportPrompt.getName(), model, prompt, msg,
                null, emitter);

        String title = splitTitle(response);
        // 保存报告
        PageReportCreateRequest reportReq = new PageReportCreateRequest();
        reportReq.setId(reportId);
        reportReq.setPageId(interpretation.getId());
        reportReq.setTitle(title);
        reportReq.setContent(response);
        reportService.create(reportReq, tenantId, username);
    }

    /**
     * 从响应中提取标题
     */
    private String splitTitle(String response) {
        // 从响应中提取标题
        return response.split("\n")[0].trim().replaceAll("^#+\\s*", "");
    }

    /**
     * 构建消息体
     */
    private Msg boxMsg(PageAssistantChatRequest req) {
        Builder msgBuilder = Msg.builder();
        msgBuilder.textContent(req.getMessage());

        Optional.ofNullable(req.getFiles())
                .ifPresent(files -> {
                    if (files.isEmpty()) {
                        return;
                    }
                    List<SysFileResponse> sysFiles = sysFileService.listByIds(files.stream().map(File::getId).toList());
                    List<String> pdfFiles = new ArrayList<>();
                    sysFiles.stream()
                            .forEach(file -> {
                                try {
                                    String path = Paths.get(uploadProperties.getStoragePath(), file.getStoragePath())
                                            .toAbsolutePath().toString();
                                    String base64 = MediaUtils.fileToBase64(path);
                                    // URLSource urlSource = URLSource.builder().url(file.getUrl()).build();
                                    Base64Source base64Source = Base64Source.builder().mediaType(file.getContentType())
                                            .data(base64).build();
                                    switch (file.getContentType()) {
                                        case "image/png", "image/jpeg":
                                            msgBuilder.content(ImageBlock.builder().source(base64Source).build());
                                            break;
                                        case "audio/wav":
                                            msgBuilder.content(AudioBlock.builder().source(base64Source).build());
                                            break;
                                        case "application/pdf":
                                            pdfFiles.add(
                                                    Paths.get(uploadProperties.getStoragePath(), file.getStoragePath())
                                                            .toAbsolutePath().toString());
                                            break;
                                        default:
                                            break;
                                    }
                                } catch (IOException e) {
                                    log.error("文件转换为 base64 失败", e);
                                }
                            });
                });
        return msgBuilder.build();
    }

    /**
     * 构建知识库消息体
     */
    private Knowledge boxKnowledge(PageAssistantChatRequest req) {

        List<File> files = req.getFiles().stream().filter(file -> ".pdf".equals(file.getExtension()))
                .toList();
        if (CollectionUtils.isEmpty(files)) {
            return null;
        }
        List<SysFileResponse> sysFiles = sysFileService.listByIds(files.stream().map(File::getId).toList());
        List<String> pdfFiles = new ArrayList<>();
        sysFiles.forEach(file -> pdfFiles.add(Paths.get(uploadProperties.getStoragePath(), file.getStoragePath())
                .toAbsolutePath().toString()));
        if (CollectionUtils.isNotEmpty(pdfFiles)) {
            // 1. 创建知识库
            EmbeddingModel embeddingModel = SpringUtils.getBean(EmbeddingModel.class);
            if (embeddingModel == null) {
                return null;
            }

            Knowledge knowledge = SimpleKnowledge.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(InMemoryStore.builder().dimensions(1024).build())
                    .build();
            PDFReader pdfReader = new PDFReader(512, SplitStrategy.SEMANTIC, 50);
            pdfFiles.forEach(file -> {
                List<Document> blocks = pdfReader
                        .read(ReaderInput.fromPath(Paths.get(file))).block();
                knowledge.addDocuments(blocks).block();
            });
            return knowledge;
        }
        return null;
    }

    /**
     * 创建页面设计
     */
    private void createPageSchemaByAgent(PageAssistantChatRequest req, ConversationResponse conversation,
            PageAssistantChatResponse response) {

        PageEntity entity = pageMapper.selectOneById(req.getBizId());

        String prompt = new String(createPageSchemaByAgentPrompt.getTemplate());
        // 页面描述
        prompt = prompt.replace("{{page_metadata}}", preparePageMetadataPrompt(req, entity));

        String tableSchemaPrompt = prepareTableSchemaPrompt(req, entity);
        String iconPrompt = prepareIconPrompt();
        prompt = prompt.replace("{{system_tables}}", tableSchemaPrompt);
        prompt = prompt.replace("{{icon_font}}", iconPrompt);

        ResultWithId<CreatePageSchemaAiAgentResponse> result = agentScopeProxy.doMemoryChat(conversation.getId(),
                createPageSchemaByAgentPrompt.getName(),
                createPageSchemaByAgentPrompt.getModel(), prompt, boxMsg(req), boxKnowledge(req),
                CreatePageSchemaAiAgentResponse.class);

        CreatePageSchemaAiAgentResponse schemaResponse = result.getResult();

        Answer answer = new Answer();
        answer.setAnswer(schemaResponse.getSummary());
        answer.setPlans(schemaResponse.getPlans());
        answer.setEffect(new Effect());
        answer.getEffect().setSchema(schemaResponse.getSchema());
        Extra extra = Extra.builder()
                .id(req.getBizId())
                .element("页面设计")
                .action("通过智能助手根据业务需求创建新的 BI 分析页面")
                .build();
        answer.getExtra().add(extra);
        response.setAnswer(answer);

        ConversationMessageCreateRequest createRequest = new ConversationMessageCreateRequest();
        createRequest.setId(result.getId());
        createRequest.setConversationId(conversation.getId());
        createRequest.setPrompt(prompt);
        createRequest.setQuestion(req.getMessage());
        createRequest.setAtItems(ObjectMapperUtils.toJson(req.getPageItems()));
        createRequest.setAnswer(ObjectMapperUtils.toJson(schemaResponse));
        if (req.getAgents() != null && !req.getAgents().isEmpty()) {
            createRequest.setAgents(req.getAgents().stream()
                    .map(f -> new ChatMessageAgent().setId(f.getId()).setName(f.getName())).toList());
            entity.setDbIds(req.getAgents().stream().map(f -> f.getId()).collect(Collectors.joining(",")));
        }
        if (req.getFiles() != null && !req.getFiles().isEmpty()) {
            createRequest.setFiles(req.getFiles().stream()
                    .map(f -> new ChatMessageFile().setId(f.getId()).setName(f.getName())).toList());
            entity.setExcelIds(req.getFiles().stream().filter(f -> f.getExtension().equals(".xlsx")).map(f -> f.getId())
                    .collect(Collectors.joining(",")));
        }
        createRequest.setExtra(ObjectMapperUtils.toJson(answer));
        ConversationMessageResponse conversationMessageResponse = conversationMessageService.create(createRequest);

        response.setId(conversationMessageResponse.getId());
        response.setCreatedAt(conversationMessageResponse.getCreatedAt());

        entity.setSchemaJson(ObjectMapperUtils.toJson(schemaResponse.getSchema()));
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        pageMapper.update(entity);
    }

    private String preparePageMetadataPrompt(PageAssistantChatRequest req, PageEntity entity) {
        String empty = "";
        return """
                <page_metadata>
                  <name>%s</name>
                  <industry>%s</industry>
                  <description>%s</description>
                </page_metadata>
                """.formatted(
                Objects.requireNonNullElse(entity.getName(), empty),
                Objects.requireNonNullElse(entity.getIndustry(), empty),
                Objects.requireNonNullElse(entity.getDescription(), empty));
    }

    /**
     * 迭代页面设计
     */
    private void iterationPageSchemaByAgent(PageAssistantChatRequest req, ConversationResponse conversation,
            PageAssistantChatResponse response) {
        PageEntity entity = pageMapper.selectOneById(req.getBizId());

        String prompt = new String(iterationPageSchemaByAgentPrompt.getTemplate());
        String currentPageSchema = prepareCurrentPageSchema(req);
        String tableSchemaPrompt = prepareTableSchemaPrompt(req, entity);
        String iconPrompt = prepareIconPrompt();
        prompt = prompt.replace("{{system_tables}}", tableSchemaPrompt);
        prompt = prompt.replace("{{icon_font}}", iconPrompt);
        prompt = prompt.replace("{{current_page_schema}}", currentPageSchema);

        ResultWithId<IterationPageSchemaAiAgentResponse> result = agentScopeProxy.doMemoryChat(conversation.getId(),
                iterationPageSchemaByAgentPrompt.getName(),
                iterationPageSchemaByAgentPrompt.getModel(), prompt, boxMsg(req), boxKnowledge(req),
                IterationPageSchemaAiAgentResponse.class);

        IterationPageSchemaAiAgentResponse schemaResponse = result.getResult();

        Answer answer = new Answer();
        answer.setAnswer(schemaResponse.getSummary());
        answer.setPlans(schemaResponse.getPlans());
        answer.setEffect(new Effect());
        answer.getEffect().setPageItems(new ArrayList<>());
        schemaResponse.getEffectItems().forEach(item -> {
            answer.getEffect().getPageItems().add(item.getPageItem());
            Extra extra = Extra.builder()
                    .id(req.getBizId())
                    .element(item.getPageItem().getPageItem().getName())
                    .action(item.getChangeNote())
                    .build();
            answer.getExtra().add(extra);
        });
        response.setAnswer(answer);

        ConversationMessageCreateRequest createRequest = new ConversationMessageCreateRequest();
        createRequest.setId(result.getId());
        createRequest.setConversationId(conversation.getId());
        createRequest.setPrompt(prompt);
        createRequest.setQuestion(req.getMessage());
        createRequest.setAtItems(ObjectMapperUtils.toJson(req.getPageItems()));
        createRequest.setAnswer(ObjectMapperUtils.toJson(schemaResponse));
        Set<String> dbIds = new HashSet<>();
        if (StringUtils.isNotBlank(entity.getDbIds())) {
            dbIds.addAll(Arrays.asList(entity.getDbIds().split(",")));
        }
        if (req.getAgents() != null && !req.getAgents().isEmpty()) {
            createRequest.setAgents(req.getAgents().stream()
                    .map(f -> new ChatMessageAgent().setId(f.getId()).setName(f.getName())).toList());
            req.getAgents().stream().forEach(f -> dbIds.add(f.getId()));
        }
        Set<String> excelIds = new HashSet<>();
        if (StringUtils.isNotBlank(entity.getExcelIds())) {
            excelIds.addAll(Arrays.asList(entity.getExcelIds().split(",")));
        }
        if (req.getFiles() != null && !req.getFiles().isEmpty()) {
            createRequest.setFiles(req.getFiles().stream()
                    .map(f -> new ChatMessageFile().setId(f.getId()).setName(f.getName())).toList());
            req.getFiles().stream().filter(f -> f.getExtension().equals(".xlsx")).forEach(f -> excelIds.add(f.getId()));
        }
        createRequest.setExtra(ObjectMapperUtils.toJson(answer));
        ConversationMessageResponse conversationMessageResponse = conversationMessageService.create(createRequest);

        response.setId(conversationMessageResponse.getId());
        response.setCreatedAt(conversationMessageResponse.getCreatedAt());

        entity.setDbIds(String.join(",", dbIds));
        entity.setExcelIds(String.join(",", excelIds));
        pageMapper.update(entity);
    }

    /**
     * 迭代页面组件
     */
    private void iterationPageItemByAgent(PageAssistantChatRequest req, ConversationResponse conversation,
            PageAssistantChatResponse response) {
        PageEntity entity = pageMapper.selectOneById(req.getBizId());

        String prompt = new String(iterationPageItemByAgentPrompt.getTemplate());
        String targetItems = prepareTargetItems(req);

        String tableSchemaPrompt = prepareTableSchemaPrompt(req, entity);
        String iconPrompt = prepareIconPrompt();
        prompt = prompt.replace("{{system_tables}}", tableSchemaPrompt);
        prompt = prompt.replace("{{icon_font}}", iconPrompt);
        prompt = prompt.replace("{{target_items}}", targetItems);

        ResultWithId<IterationPageSchemaAiAgentResponse> result = agentScopeProxy.doMemoryChat(conversation.getId(),
                iterationPageItemByAgentPrompt.getName(),
                iterationPageItemByAgentPrompt.getModel(), prompt, boxMsg(req), boxKnowledge(req),
                IterationPageSchemaAiAgentResponse.class);

        IterationPageSchemaAiAgentResponse schemaResponse = result.getResult();

        Answer answer = new Answer();
        answer.setAnswer(schemaResponse.getSummary());
        answer.setPlans(schemaResponse.getPlans());
        answer.setEffect(new Effect());
        answer.getEffect().setPageItems(new ArrayList<>());
        schemaResponse.getEffectItems().forEach(item -> {
            answer.getEffect().getPageItems().add(item.getPageItem());
            Extra extra = Extra.builder()
                    .id(req.getBizId())
                    .element(item.getPageItem().getPageItem().getName())
                    .action(item.getChangeNote())
                    .build();
            answer.getExtra().add(extra);
        });
        response.setAnswer(answer);

        ConversationMessageCreateRequest createRequest = new ConversationMessageCreateRequest();
        createRequest.setId(result.getId());
        createRequest.setConversationId(conversation.getId());
        createRequest.setPrompt(prompt);
        createRequest.setQuestion(req.getMessage());
        createRequest.setAtItems(ObjectMapperUtils.toJson(req.getPageItems()));
        createRequest.setAnswer(ObjectMapperUtils.toJson(schemaResponse));
        Set<String> dbIds = new HashSet<>();
        if (StringUtils.isNotBlank(entity.getDbIds())) {
            dbIds.addAll(Arrays.asList(entity.getDbIds().split(",")));
        }
        if (req.getAgents() != null && !req.getAgents().isEmpty()) {
            createRequest.setAgents(req.getAgents().stream()
                    .map(f -> new ChatMessageAgent().setId(f.getId()).setName(f.getName())).toList());
            req.getAgents().forEach(f -> dbIds.add(f.getId()));
        }
        Set<String> excelIds = new HashSet<>();
        if (StringUtils.isNotBlank(entity.getExcelIds())) {
            excelIds.addAll(Arrays.asList(entity.getExcelIds().split(",")));
        }
        if (req.getFiles() != null && !req.getFiles().isEmpty()) {
            createRequest.setFiles(req.getFiles().stream()
                    .map(f -> new ChatMessageFile().setId(f.getId()).setName(f.getName())).toList());
            req.getFiles().stream().filter(f -> f.getExtension().equals(".excel"))
                    .forEach(f -> excelIds.add(f.getId()));
        }
        createRequest.setExtra(ObjectMapperUtils.toJson(answer));
        ConversationMessageResponse conversationMessageResponse = conversationMessageService.create(createRequest);

        response.setId(conversationMessageResponse.getId());
        response.setCreatedAt(conversationMessageResponse.getCreatedAt());

        entity.setDbIds(String.join(",", dbIds));
        entity.setExcelIds(String.join(",", excelIds));
        pageMapper.update(entity);
    }

    public CachedExcelDatabase loadExcelDatabase(String id) {
        Path path = Paths.get(chatFileDbPath, "bi", id + ".mv.db");
        if (!path.toFile().exists()) {
            return null;
        }
        String dbFilePath = path
                .toAbsolutePath()
                .toString();
        try {
            return CachedExcelDatabase.load(dbFilePath.substring(0, dbFilePath.length() - 6));
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 准备数据结构提示词
     */
    private String prepareTableSchemaPrompt(PageAssistantChatRequest req, PageEntity entity) {
        String dbFilePath = Paths.get(chatFileDbPath, "bi", req.getBizId())
                .toAbsolutePath()
                .toString();
        List<String> excels = new ArrayList<>();
        List<File> uploadFiles = Objects.requireNonNullElseGet(req.getFiles(), ArrayList::new);
        excels.addAll(uploadFiles.stream().filter(f -> ".xlsx".equals(f.getExtension())).map(File::getId).toList());
        if (StringUtils.isNotBlank(entity.getExcelIds())) {
            List<String> ids = Arrays.stream(entity.getExcelIds().split(",")).toList();
            excels.addAll(ids);
        }
        List<DatasourceDbEntity> dbs = new ArrayList<>();

        if (entity != null && StringUtils.isNotBlank(entity.getDbIds())) {
            List<String> dbIds = Arrays.stream(entity.getDbIds().split(","))
                    .filter(id -> dbs.stream().noneMatch(db -> db.getId().equals(id))).toList();
            dbs.addAll(datasourceService.activeListDbs(dbIds));
        }
        if (!CollectionUtils.isEmpty(req.getAgents())) {
            dbs.addAll(datasourceService.activeListDbs(req.getAgents().stream().map(Agent::getId).toList()));
        } else if (dbs.isEmpty() && StringUtils.isBlank(entity.getExcelIds()) && CollectionUtils.isEmpty(excels)) {
            dbs.addAll(datasourceService.activeListDbs());
        }
        StringJoiner join = new StringJoiner("", "<databases>", "</databases>");
        List<String> dbIds = dbs.stream().map(DatasourceDbEntity::getId).toList();
        Map<String, List<DatasourceTableEntity>> tables = datasourceService.activeListTables(dbIds).stream()
                .collect(Collectors.groupingBy(DatasourceTableEntity::getDbId));
        Map<String, List<DatasourceFieldEntity>> fields = datasourceService.activeListFields(dbIds).stream()
                .collect(Collectors.groupingBy(DatasourceFieldEntity::getTableId));
        dbs.forEach(db -> {
            StringJoiner dbJoin = new StringJoiner("", "<database name=\"%s\" comment=\"%s\"><tables>".formatted(
                    db.getName(), Objects.requireNonNullElse(db.getDescription(), "")), "</tables></database>");

            tables.get(db.getId()).forEach(table -> {
                StringJoiner tableJoin = new StringJoiner("", "<table name=\"%s\" comment=\"%s\"><fields>".formatted(
                        table.getName(), Objects.requireNonNullElse(table.getDescription(), "")), "</fields></table>");

                fields.get(table.getId()).forEach(field -> tableJoin.add("<field name=\"%s\" comment=\"%s\" />"
                        .formatted(field.getName(), Objects.requireNonNullElse(field.getDescription(), ""))));

                dbJoin.add(tableJoin.toString());
            });

            join.add(dbJoin.toString());
        });

        if (CollectionUtils.isNotEmpty(excels)) {
            List<String> filePaths = sysFileService.listByIds(excels).stream()
                    .map(file -> Paths.get(uploadProperties.getStoragePath(), file.getStoragePath()).toAbsolutePath()
                            .toString())
                    .toList();
            try (CachedExcelDatabase fileDb = ExcelUtils.importExcelToH2(filePaths, dbFilePath)) {

                StringJoiner dbJoin = new StringJoiner("",
                        "<database name=\"用户提交数据库\" comment=\"用户上传的Excel文件\"><tables>",
                        "</tables></database>");
                fileDb.getExcelSchemas().forEach(file -> {
                    String fileName = file.getFileName();
                    file.getSheetSchemas().forEach(sheet -> {
                        String sheetName = sheet.getSheetName();
                        String tableName = sheet.getTableName();
                        StringJoiner tableJoin = new StringJoiner("",
                                "<table name=\"%s\" comment=\"%s\"><fields>".formatted(
                                        tableName, fileName + " --> " + sheetName),
                                "</fields></table>");

                        sheet.getColumnMapping()
                                .forEach((comment, name) -> tableJoin
                                        .add("<field name=\"%s\" comment=\"%s\" />".formatted(name, comment)));
                        dbJoin.add(tableJoin.toString());
                    });
                });
                join.add(dbJoin.toString());
            }

        }

        return join.toString();
    }

    /**
     * 准备图标字体提示词
     */
    private String prepareIconPrompt() {
        SysIconQueryRequest req = new SysIconQueryRequest();
        List<SysIconResponse> icons = sysIconService.listAll(req);
        String iconTemplate = "<IconFont name=\"%s\" code=\"icon-%s\" />";
        StringJoiner join = new StringJoiner("", "<IconFonts>", "</IconFonts>");
        icons.forEach(i -> join.add(iconTemplate.formatted(i.getName(), i.getId())));
        return join.toString();
    }

    /**
     * 准备当前页面结构提示词
     */
    private String prepareCurrentPageSchema(PageAssistantChatRequest req) {
        return "<current_page_schema_json>%s</current_page_schema_json>"
                .formatted(Objects.requireNonNullElse(req.getPage(), ""));
    }

    /**
     * 准备目标页面结构项提示词
     */
    private String prepareTargetItems(PageAssistantChatRequest req) {
        return "<target_page_items_json>%s</target_page_items_json>"
                .formatted(Objects.requireNonNullElse(req.getPageItems(), ""));
    }

    /**
     * 页面结构AI响应
     */
    @Data
    public static class CreatePageSchemaAiResponse {
        @Description("页面结构")
        private PageSchema schema;
        @Description("对页面结构的自然语言总结说明")
        private String summary;
    }

    /**
     * 页面结构AI响应
     */
    @Data
    public static class CreatePageSchemaAiAgentResponse {
        @Description("页面结构")
        private PageSchema schema;
        @Description("对页面结构的自然语言总结说明")
        private String summary;
        @Description("变更计划")
        private List<Plan> plans;
    }

    /**
     * 页面结构AI响应
     */
    @Data
    private static class IterationPageSchemaAiResponse {
        @Description("页面结构")
        private List<IterationPageItem> effectItems;
        @Description("对页面结构的自然语言总结说明")
        private String summary;
    }

    /**
     * 页面结构AI响应
     */
    @Data
    private static class IterationPageSchemaAiAgentResponse {
        @Description("页面结构")
        private List<IterationPageItem> effectItems;
        @Description("对页面结构的自然语言总结说明")
        private String summary;
        @Description("变更计划")
        private List<Plan> plans;
    }

    /**
     * 页面结构变更项
     */
    @Data
    private static class IterationPageItem {
        @Description("页面结构项")
        private EffectSchemaItem pageItem;
        @Description("变更说明")
        private String changeNote;
    }

    @SuppressWarnings("unchecked")
    public static void itemConsumer(SchemaItem item) {
        item.setId(IdUtils.randomId());
        if (item.getType().equals("b-icon")) {
            item.setDatasource(null);
            item.setCascadeIds(null);
            Map<String, Object> props = item.getProps();
            if (props.get("classNames") instanceof List classNames) {
                if (classNames.stream().anyMatch(i -> i.toString().matches("^w-\\d+$"))) {
                    // classNames.removeIf(c -> c.toString().matches("bg-[a-z]+-\\d+"));
                    String w = null;
                    for (int i = 0; i < classNames.size(); i++) {
                        String className = classNames.get(i).toString();
                        // if (className.matches("^text-[a-z]+-\\d+$")) {
                        //     classNames.set(i, className.replaceFirst("^text-([a-z]+)-(\\d+)$", "bg-$1-$2"));
                        // }
                        if (className.matches("^w-\\d+$")) {
                            w = className;
                        }
                    }
                    if (w != null) classNames.add("min-" + w);
                    SchemaItem clone = ObjectMapperUtils.fromJson(ObjectMapperUtils.toJson(item), SchemaItem.class);
                    props.clear();
                    props.put("classNames", classNames);
                    item.setType("b-container");
                    item.setId(IdUtils.randomId());
                    item.setName(item.getName() + "容器");
                    item.setChildren(List.of(clone));
                    
                    clone.getProps().put("classNames", List.of("text-lg"));
                }
            }
        }
        if (List.of("b-html", "b-echarts").contains(item.getType()) && item.getDatasource() == null) {
            SchemaItemDatasource ds = new SchemaItemDatasource();
            ds.setSource("custom");
            ds.setCustom("{}");
            item.setDatasource(ds);
        }
        if (item.getChildren() instanceof List list) {
            list.forEach(i -> itemConsumer((SchemaItem) i));
        }
        if (item.getChildren() instanceof Map map) {
            map.values().forEach(i -> itemConsumer((SchemaItem) i));
        }
    }

    public static void main(String[] args) {
        // for (int i = 0; i < 100; i++) {
        // System.out.println(IdUtils.randomId());
        // }
        try {
            String schema = Files.readString(Path.of("D:\\Users\\eaves\\Downloads\\schema.json"),
                    StandardCharsets.UTF_8);
            PageSchema pageSchema = ObjectMapperUtils.fromJson(schema, PageSchema.class);

            pageSchema.getItems().forEach(PageService::itemConsumer);
            Files.writeString(Path.of("D:\\Users\\eaves\\Downloads\\schema-new.json"),
                    ObjectMapperUtils.toJson(pageSchema), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
