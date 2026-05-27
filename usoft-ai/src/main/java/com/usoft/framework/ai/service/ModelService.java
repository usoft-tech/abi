package com.usoft.framework.ai.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.ai.api.ModelCreateRequest;
import com.usoft.framework.ai.api.ModelQueryRequest;
import com.usoft.framework.ai.api.ModelResponse;
import com.usoft.framework.ai.api.ModelUpdateRequest;
import com.usoft.framework.ai.entity.ModelEntity;
import com.usoft.framework.ai.mapper.ModelMapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.utils.ObjectMapperUtils;

@Service
public class ModelService {
    private final ModelMapper modelMapper;

    public ModelService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // @CacheEvict(value = { "aiChatModel", "aiModels" }, allEntries = true)
    public ModelResponse create(ModelCreateRequest req) {
        ModelEntity e = new ModelEntity();
        BeanMapper.mapper(req, e);
        if (req.getExtProps() != null) {
            e.setExtProps(ObjectMapperUtils.toJson(req.getExtProps()));
        }
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        modelMapper.insert(e);
        return toResponse(e);
    }

    // @CacheEvict(value = { "aiChatModel", "aiModels" }, allEntries = true)
    public ModelResponse update(String id, ModelUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        ModelEntity e = modelMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        BeanMapper.mapper(req, e);
        if (req.getExtProps() != null) {
            e.setExtProps(ObjectMapperUtils.toJson(req.getExtProps()));
        }
        if (req.getApiKey() != null && !req.getApiKey().isBlank()) {
            e.setApiKey(req.getApiKey());
        }
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        modelMapper.update(e);
        return toResponse(e);
    }

    // @CacheEvict(value = { "aiChatModel", "aiModels" }, allEntries = true)
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        ModelEntity e = modelMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return modelMapper.update(e) > 0;
    }

    public ModelResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        ModelEntity e = modelMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        return toResponse(e);
    }

    public PageResponse<ModelResponse> list(ModelQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getProvider() != null && !req.getProvider().isBlank()) {
            qw.and("provider = ?", req.getProvider());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ? OR provider LIKE ? OR model LIKE ?)", kw, kw, kw);
        }
        return PageHelper.apply(modelMapper, qw, req, this::toResponse);
    }

    private ModelResponse toResponse(ModelEntity e) {
        ModelResponse r = new ModelResponse();
        BeanMapper.mapper(e, r);
        r.setExtProps(ObjectMapperUtils.fromJson(e.getExtProps(), new TypeReference<Map<String, Object>>() {
        }));
        return r;
    }

}
