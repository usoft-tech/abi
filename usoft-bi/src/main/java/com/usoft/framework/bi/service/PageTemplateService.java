package com.usoft.framework.bi.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.PageTemplateCreateRequest;
import com.usoft.framework.bi.api.PageTemplateResponse;
import com.usoft.framework.bi.api.PageTemplateUpdateRequest;
import com.usoft.framework.bi.api.page.schema.PageSchema;
import com.usoft.framework.bi.entity.PageTemplateEntity;
import com.usoft.framework.bi.mapper.PageTemplateMapper;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.utils.ObjectMapperUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 页面模板服务类
 */
@Service
@RequiredArgsConstructor
public class PageTemplateService {
    private final PageTemplateMapper mapper;

    /**
     * 分页查询页面模板
     */
    public PageResponse<PageTemplateResponse> list(int page, int size, String keyword) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (StringUtils.isNotBlank(keyword)) {
            String kw = "%" + keyword + "%";
            qw.and("(name LIKE ? OR description LIKE ?)", kw, kw);
        }
        qw.orderBy("created_at", false);

        PageRequest req = new PageRequest();
        req.setPage(page);
        req.setSize(size);

        return PageHelper.apply(mapper, qw, req, this::toResponse);
    }

    /**
     * 创建页面模板
     */
    public PageTemplateResponse create(PageTemplateCreateRequest req) {
        PageTemplateEntity entity = new PageTemplateEntity();
        BeanMapper.mapper(req, entity);
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantId(TenantContext.getTenantId());
        entity.setIsDeleted(false);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(UserHolder.username());
        if (req.getSchema() != null) {
            entity.setSchemaJson(ObjectMapperUtils.toJson(req.getSchema()));
        }
        mapper.insert(entity);
        return toResponse(entity);
    }

    /**
     * 更新页面模板
     */
    public PageTemplateResponse update(PageTemplateUpdateRequest req) {
        PageTemplateEntity entity = mapper.selectOneById(req.getId());
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return null;
        }
        BeanMapper.mapper(req, entity);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        if (req.getSchema() != null) {
            entity.setSchemaJson(ObjectMapperUtils.toJson(req.getSchema()));
        }
        mapper.update(entity);
        return toResponse(entity);
    }

    /**
     * 删除页面模板
     */
    public void delete(String id) {
        PageTemplateEntity entity = mapper.selectOneById(id);
        if (entity != null) {
            entity.setIsDeleted(true);
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(UserHolder.username());
            mapper.update(entity);
        }
    }

    /**
     * 获取页面模板详情
     */
    public PageTemplateResponse get(String id) {
        PageTemplateEntity entity = mapper.selectOneById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return null;
        }
        return toResponse(entity);
    }

    private PageTemplateResponse toResponse(PageTemplateEntity entity) {
        PageTemplateResponse resp = new PageTemplateResponse();
        BeanMapper.mapper(entity, resp);
        if (StringUtils.isNotBlank(entity.getSchemaJson())) {
            resp.setSchema(ObjectMapperUtils.fromJson(entity.getSchemaJson(), PageSchema.class));
        }
        return resp;
    }
}
