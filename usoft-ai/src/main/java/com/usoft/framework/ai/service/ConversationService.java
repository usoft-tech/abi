package com.usoft.framework.ai.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.usoft.framework.ai.api.ConversationQueryRequest;
import com.usoft.framework.ai.api.ConversationResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.ai.entity.ConversationEntity;
import com.usoft.framework.ai.mapper.ConversationMapper;

@Service
public class ConversationService {
    private final ConversationMapper conversationMapper;

    public ConversationService(ConversationMapper conversationMapper) {
        this.conversationMapper = conversationMapper;
    }

    /**
     * 创建会话
     */
    public ConversationResponse create(String bizType, String bizId, String message) {
        ConversationEntity e = new ConversationEntity();
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setBizType(bizType);
        e.setBizId(bizId);
        e.setName(message);
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        conversationMapper.insert(e);
        ConversationResponse r = new ConversationResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    /**
     * 删除会话
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        ConversationEntity e = conversationMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        e.setIsDeleted(true);
        return conversationMapper.update(e) > 0;
    }

    /**
     * 查询会话列表
     */
    public List<ConversationResponse> list(ConversationQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");

        if (req.getPrevId() != null && !req.getPrevId().isBlank()) {
            ConversationEntity prev = conversationMapper.selectOneById(req.getPrevId());
            if (prev != null) {
                qw.and("created_at < ?", prev.getCreatedAt());
            }
        }

        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getBizType() != null && !req.getBizType().isBlank()) {
            qw.and("biz_type = ?", req.getBizType());
        }
        if (req.getBizId() != null && !req.getBizId().isBlank()) {
            qw.and("biz_id = ?", req.getBizId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ? OR biz_type LIKE ? OR biz_id LIKE ?)", kw, kw, kw);
        }

        qw.orderBy("created_at", false);
        qw.limit(req.getLimit() != null ? req.getLimit() : 20);

        return conversationMapper.selectListByQuery(qw).stream().map(e -> {
            ConversationResponse r = new ConversationResponse();
            BeanMapper.mapper(e, r);
            return r;
        }).toList();
    }

    public ConversationResponse get(String conversationId) {
        String tenantId = TenantContext.getTenantId();
        ConversationEntity e = conversationMapper.selectOneById(conversationId);
        if (e == null || !StringUtils.equals(tenantId, e.getTenantId())) {
            return null;
        }
        ConversationResponse r = new ConversationResponse();
        BeanMapper.mapper(e, r);
        return r;
    }

    @Transactional
    public void active(ConversationResponse conversation) {
        UpdateChain.of(ConversationEntity.class)
                .set(ConversationEntity::getIsActived, false)
                .eq(ConversationEntity::getBizId, conversation.getBizId())
                .eq(ConversationEntity::getBizType, conversation.getBizType())
                .eq(ConversationEntity::getIsActived, true)
                .ne(ConversationEntity::getId, conversation.getId())
                .update();
        UpdateChain.of(ConversationEntity.class)
                .set(ConversationEntity::getIsActived, true)
                .eq(ConversationEntity::getId, conversation.getId())
                .update();
    }

    @Transactional
    public boolean activate(String id) {
        ConversationResponse conversation = get(id);
        if (conversation == null) {
            return false;
        }
        active(conversation);
        return true;
    }
}
