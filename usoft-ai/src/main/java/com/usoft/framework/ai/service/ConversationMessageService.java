package com.usoft.framework.ai.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.ai.api.ChatMessageAgent;
import com.usoft.framework.ai.api.ChatMessageFile;
import com.usoft.framework.ai.api.ConversationMessageCreateRequest;
import com.usoft.framework.ai.api.ConversationMessageQueryRequest;
import com.usoft.framework.ai.api.ConversationMessageResponse;
import com.usoft.framework.ai.api.ConversationResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.ai.AgentScopeProxy;
import com.usoft.framework.ai.entity.ConversationMessageEntity;
import com.usoft.framework.ai.mapper.ConversationMessageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationMessageService {
    private final ConversationMessageMapper messageMapper;
    private final ConversationService conversationService;
    private final AgentScopeProxy agentScopeProxy;

    /**
     * 创建会话消息
     */
    public ConversationMessageResponse create(
            ConversationMessageCreateRequest req) {
        ConversationMessageEntity e = new ConversationMessageEntity();
        BeanMapper.mapper(req, e);
        if (e.getId() == null || e.getId().isBlank()) {
            e.setId(UUID.randomUUID().toString());
        } 
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        e.setAgents(ObjectMapperUtils.toJson(req.getAgents()));
        e.setFiles(ObjectMapperUtils.toJson(req.getFiles()));
        e.setExtraProps(req.getExtra());
        messageMapper.insert(e);
        ConversationMessageResponse r = new ConversationMessageResponse();
        BeanMapper.mapper(e, r);
        r.setExtra(e.getExtraProps());
        return r;
    }

    /**
     * 删除会话消息
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        ConversationMessageEntity e = messageMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }

        agentScopeProxy.deleteMessage(e.getConversationId(), e.getId());
        
        e.setIsDeleted(true);
        return messageMapper.update(e) > 0;
    }

    /**
     * 查询会话消息列表
     */
    public List<ConversationMessageResponse> list(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        // 检查会话是否存在
        ConversationResponse conversation = conversationService.get(conversationId);
        if (conversation == null) {
            return List.of();
        }

        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");

        if (conversationId != null && !conversationId.isBlank()) {
            qw.and("conversation_id = ?", conversationId);
        }

        qw.orderBy("created_at", true);

        return messageMapper.selectListByQuery(qw)
                .stream()
                .map(e -> {
                    ConversationMessageResponse r = new ConversationMessageResponse();
                    BeanMapper.mapper(e, r);
                    r.setAgents(ObjectMapperUtils.fromJson(e.getAgents(), new TypeReference<List<ChatMessageAgent>>() {
                    }));
                    r.setFiles(ObjectMapperUtils.fromJson(e.getFiles(), new TypeReference<List<ChatMessageFile>>() {
                    }));
                    r.setExtra(e.getExtraProps());
                    return r;
                })
                .toList();
    }

    /**
     * 查询会话消息列表
     */
    @Transactional
    public List<ConversationMessageResponse> list(ConversationMessageQueryRequest req) {
        if (req.getConversationId() == null || req.getConversationId().isBlank()) {
            return List.of();
        }

        // 检查会话是否存在
        ConversationResponse conversation = conversationService.get(req.getConversationId());
        if (conversation == null) {
            return List.of();
        }
        conversationService.active(conversation);

        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");

        if (req.getPrevId() != null && !req.getPrevId().isBlank()) {
            ConversationMessageEntity prev = messageMapper.selectOneById(req.getPrevId());
            if (prev != null) {
                qw.and("created_at < ?", prev.getCreatedAt());
            }
        }

        if (req.getConversationId() != null && !req.getConversationId().isBlank()) {
            qw.and("conversation_id = ?", req.getConversationId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(question LIKE ? OR prompt LIKE ? OR answer LIKE ?)", kw, kw, kw);
        }

        qw.orderBy("created_at", false);
        qw.limit(req.getLimit() != null ? req.getLimit() : 20);

        return messageMapper.selectListByQuery(qw)
                .stream()
                .map(e -> {
                    ConversationMessageResponse r = new ConversationMessageResponse();
                    BeanMapper.mapper(e, r);
                    r.setAgents(ObjectMapperUtils.fromJson(e.getAgents(), new TypeReference<List<ChatMessageAgent>>() {
                    }));
                    r.setFiles(ObjectMapperUtils.fromJson(e.getFiles(), new TypeReference<List<ChatMessageFile>>() {
                    }));
                    r.setExtra(e.getExtraProps());
                    return r;
                })
                .toList();
    }
}
