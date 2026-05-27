package com.usoft.framework.system.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.api.DepartmentCreateRequest;
import com.usoft.framework.system.api.DepartmentQueryRequest;
import com.usoft.framework.system.api.DepartmentResponse;
import com.usoft.framework.system.api.DepartmentUpdateRequest;
import com.usoft.framework.system.entity.DepartmentEntity;
import com.usoft.framework.system.mapper.DepartmentMapper;
import com.usoft.framework.common.api.PageResponse;

/**
 * 部门服务
 */
@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;

    /**
     * 构造服务
     */
    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    /**
     * 创建部门
     */
    public DepartmentResponse create(DepartmentCreateRequest req) {
        DepartmentEntity e = new DepartmentEntity();
        e.setId(UUID.randomUUID().toString());
        e.setTenantId(TenantContext.getTenantId());
        e.setName(req.getName());
        e.setParentId(req.getParentId());
        e.setSort(req.getSort());
        if (e.getParentId() == null || e.getParentId().isBlank()) {
            e.setAncestorIds("");
            e.setAncestorSorts("");
            e.setLevel(1);
        } else {
            DepartmentEntity parent = departmentMapper.selectOneById(e.getParentId());
            if (parent != null) {
                String prefixIds = parent.getAncestorIds() == null ? "" : parent.getAncestorIds();
                String prefixSorts = parent.getAncestorSorts() == null ? "" : parent.getAncestorSorts();
                e.setAncestorIds(prefixIds.isBlank() ? parent.getId() : prefixIds + "," + parent.getId());
                e.setAncestorSorts(prefixSorts.isBlank() ? String.valueOf(parent.getSort() == null ? 0 : parent.getSort())
                        : prefixSorts + "," + (parent.getSort() == null ? 0 : parent.getSort()));
                e.setLevel(parent.getLevel() == null ? 2 : parent.getLevel() + 1);
                parent.setIsLeaf(false);
                departmentMapper.update(parent);
            } else {
                e.setAncestorIds("");
                e.setAncestorSorts("");
                e.setLevel(1);
            }
        }
        e.setIsLeaf(true);
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        departmentMapper.insert(e);
        DepartmentResponse r = new DepartmentResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setParentId(e.getParentId());
        r.setAncestorIds(e.getAncestorIds());
        r.setSort(e.getSort());
        r.setAncestorSorts(e.getAncestorSorts());
        r.setLevel(e.getLevel());
        r.setIsLeaf(e.getIsLeaf());
        return r;
    }

    /**
     * 更新部门
     */
    public DepartmentResponse update(String id, DepartmentUpdateRequest req) {
        String tenantId = TenantContext.getTenantId();
        DepartmentEntity e = departmentMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        e.setName(req.getName());
        e.setParentId(req.getParentId());
        e.setSort(req.getSort());
        if (e.getParentId() == null || e.getParentId().isBlank()) {
            e.setAncestorIds("");
            e.setAncestorSorts("");
            e.setLevel(1);
        } else {
            DepartmentEntity parent = departmentMapper.selectOneById(e.getParentId());
            if (parent != null) {
                String prefixIds = parent.getAncestorIds() == null ? "" : parent.getAncestorIds();
                String prefixSorts = parent.getAncestorSorts() == null ? "" : parent.getAncestorSorts();
                e.setAncestorIds(prefixIds.isBlank() ? parent.getId() : prefixIds + "," + parent.getId());
                e.setAncestorSorts(prefixSorts.isBlank() ? String.valueOf(parent.getSort() == null ? 0 : parent.getSort())
                        : prefixSorts + "," + (parent.getSort() == null ? 0 : parent.getSort()));
                e.setLevel(parent.getLevel() == null ? 2 : parent.getLevel() + 1);
                parent.setIsLeaf(false);
                departmentMapper.update(parent);
            } else {
                e.setAncestorIds("");
                e.setAncestorSorts("");
                e.setLevel(1);
            }
        }
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        departmentMapper.update(e);
        DepartmentResponse r = new DepartmentResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setParentId(e.getParentId());
        r.setAncestorIds(e.getAncestorIds());
        r.setSort(e.getSort());
        r.setAncestorSorts(e.getAncestorSorts());
        r.setLevel(e.getLevel());
        r.setIsLeaf(e.getIsLeaf());
        return r;
    }

    /**
     * 删除部门
     */
    public boolean delete(String id) {
        String tenantId = TenantContext.getTenantId();
        DepartmentEntity e = departmentMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return departmentMapper.update(e) > 0;
    }

    /**
     * 查询部门详情
     */
    public DepartmentResponse get(String id) {
        String tenantId = TenantContext.getTenantId();
        DepartmentEntity e = departmentMapper.selectOneById(id);
        if (e == null || !tenantId.equals(e.getTenantId())) {
            return null;
        }
        DepartmentResponse r = new DepartmentResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        return r;
    }

    /**
     * 列出当前租户的部门（分页筛选）
     */
    public PageResponse<DepartmentResponse> list(DepartmentQueryRequest req) {
        String tenantId = TenantContext.getTenantId();
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        if (req.getName() != null && !req.getName().isBlank()) {
            qw.and("name LIKE ?", "%" + req.getName() + "%");
        }
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            qw.and("parent_id = ?", req.getParentId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(name LIKE ?)", kw);
        }
        return PageHelper.apply(departmentMapper, qw, req, e -> {
            DepartmentResponse r = new DepartmentResponse();
            r.setId(e.getId());
            r.setName(e.getName());
            r.setParentId(e.getParentId());
            r.setAncestorIds(e.getAncestorIds());
            r.setSort(e.getSort());
            r.setAncestorSorts(e.getAncestorSorts());
            r.setLevel(e.getLevel());
            r.setIsLeaf(e.getIsLeaf());
            return r;
        });
    }
    
}
