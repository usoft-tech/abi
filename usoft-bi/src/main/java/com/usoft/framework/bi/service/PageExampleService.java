package com.usoft.framework.bi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.PageExampleCreateRequest;
import com.usoft.framework.bi.api.PageExampleResponse;
import com.usoft.framework.bi.api.PageExampleUpdateRequest;
import com.usoft.framework.bi.api.page.schema.SchemaItem;
import com.usoft.framework.bi.entity.PageExampleEntity;
import com.usoft.framework.bi.mapper.PageExampleMapper;
import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.utils.ObjectMapperUtils;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 页面片段服务类
 */
@Service
@RequiredArgsConstructor
public class PageExampleService {
    private final PageExampleMapper mapper;

    /**
     * 分页查询页面片段
     */
    public PageResponse<PageExampleResponse> list(int page, int size, String keyword) {
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
     * 创建页面片段
     */
    public PageExampleResponse create(PageExampleCreateRequest req) {
        PageExampleEntity entity = new PageExampleEntity();
        BeanMapper.mapper(req, entity);
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantId(TenantContext.getTenantId());
        entity.setIsDeleted(false);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(UserHolder.username());
        if (req.getItem() != null) {
            entity.setItemJson(ObjectMapperUtils.toJson(req.getItem()));
        }
        mapper.insert(entity);
        return toResponse(entity);
    }

    /**
     * 更新页面片段
     */
    public PageExampleResponse update(PageExampleUpdateRequest req) {
        PageExampleEntity entity = mapper.selectOneById(req.getId());
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return null;
        }
        BeanMapper.mapper(req, entity);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());
        if (req.getItem() != null) {
            entity.setItemJson(ObjectMapperUtils.toJson(req.getItem()));
        }
        mapper.update(entity);
        return toResponse(entity);
    }

    /**
     * 删除页面片段
     */
    public void delete(String id) {
        PageExampleEntity entity = mapper.selectOneById(id);
        if (entity != null) {
            entity.setIsDeleted(true);
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(UserHolder.username());
            mapper.update(entity);
        }
    }

    /**
     * 获取页面片段详情
     */
    public PageExampleResponse get(String id) {
        PageExampleEntity entity = mapper.selectOneById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return null;
        }
        return toResponse(entity);
    }

    private PageExampleResponse toResponse(PageExampleEntity entity) {
        PageExampleResponse resp = new PageExampleResponse();
        BeanMapper.mapper(entity, resp);
        if (StringUtils.isNotBlank(entity.getItemJson())) {
            resp.setItem(ObjectMapperUtils.fromJson(entity.getItemJson(), SchemaItem.class));
        }
        return resp;
    }
}
