package com.usoft.framework.bi.service;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.api.PageReportCreateRequest;
import com.usoft.framework.bi.api.PageReportQueryRequest;
import com.usoft.framework.bi.api.PageReportResponse;
import com.usoft.framework.bi.api.PageReportUpdateRequest;
import com.usoft.framework.bi.entity.PageReportEntity;
import com.usoft.framework.bi.mapper.PageReportMapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;

import lombok.RequiredArgsConstructor;

/**
 * 页面报告服务
 */
@Service
@RequiredArgsConstructor
public class PageReportService {

    private final PageReportMapper reportMapper;

    /**
     * 创建页面报告
     *
     * @param req 创建请求
     * @return 响应
     */
    @Transactional(rollbackFor = Exception.class)
    public PageReportResponse create(PageReportCreateRequest req, String tenantId, String username) {
        PageReportEntity entity = new PageReportEntity();
        BeanMapper.mapper(req, entity);

        if (StringUtils.isNotBlank(req.getId())) {
            entity.setId(UUID.randomUUID().toString());
        }
        entity.setTenantId(tenantId);
        entity.setIsDeleted(false);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy(username);

        reportMapper.insert(entity);
        return toResponse(entity);
    }

    /**
     * 更新页面报告
     *
     * @param id  ID
     * @param req 更新请求
     * @return 响应
     */
    @Transactional(rollbackFor = Exception.class)
    public PageReportResponse update(String id, PageReportUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        PageReportEntity entity = reportMapper.findByIdInTenant(id, tenantId);
        if (entity == null) {
            return null;
        }

        BeanMapper.mapper(req, entity);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(UserHolder.username());

        reportMapper.update(entity);
        return toResponse(entity);
    }

    /**
     * 获取页面报告详情
     *
     * @param id ID
     * @return 响应
     */
    public PageReportResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        PageReportEntity entity = reportMapper.findByIdInTenant(id, tenantId);
        if (entity == null) {
            return null;
        }
        return toResponse(entity);
    }

    /**
     * 根据页面ID查询报告列表
     *
     * @param pageId 页面ID
     * @return 报告列表
     */
    public PageResponse<PageReportResponse> listByPageId(PageReportQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("page_id = ?", req.getPageId())
                .and("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)")
                .orderBy("created_at", false);

        return PageHelper.apply(reportMapper, qw, req, this::toResponse);
    }

    /**
     * 删除页面报告
     *
     * @param id ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String tenantId = TenantContext.getTenantId();
        PageReportEntity entity = reportMapper.findByIdInTenant(id, tenantId);
        if (entity != null) {
            entity.setIsDeleted(true);
            entity.setUpdatedAt(Instant.now());
            entity.setUpdatedBy(UserHolder.username());
            reportMapper.update(entity);
        }
    }

    /**
     * 转换为响应对象
     *
     * @param entity 实体
     * @return 响应
     */
    private PageReportResponse toResponse(PageReportEntity entity) {
        PageReportResponse response = new PageReportResponse();
        BeanMapper.mapper(entity, response);
        return response;
    }
}
