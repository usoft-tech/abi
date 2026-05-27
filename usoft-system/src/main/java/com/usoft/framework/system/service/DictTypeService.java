package com.usoft.framework.system.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.enums.EnableStatus;
import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.system.api.DictTypeCreateRequest;
import com.usoft.framework.system.api.DictTypeQueryRequest;
import com.usoft.framework.system.api.DictTypeResponse;
import com.usoft.framework.system.api.DictTypeUpdateRequest;
import com.usoft.framework.system.entity.DictTypeEntity;
import com.usoft.framework.system.mapper.DictTypeMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * 字典类型服务
 */
@Service
public class DictTypeService {
    private final DictTypeMapper dictTypeMapper;

    public DictTypeService(DictTypeMapper dictTypeMapper) {
        this.dictTypeMapper = dictTypeMapper;
    }

    /**
     * 创建字典类型
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public DictTypeResponse create(DictTypeCreateRequest req) {
        QueryWrapper qw = QueryWrapper.create().where("dict_type = ?", req.getDictType());
        if (dictTypeMapper.selectCountByQuery(qw) > 0) {
            throw new BizException("字典类型已存在");
        }
        DictTypeEntity e = new DictTypeEntity();
        e.setId(UUID.randomUUID().toString());
        e.setDictName(req.getDictName());
        e.setDictType(req.getDictType());
        e.setStatus(req.getStatus() != null ? req.getStatus() : EnableStatus.ENABLE);
        e.setRemark(req.getRemark());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        dictTypeMapper.insert(e);
        return toResponse(e);
    }

    /**
     * 更新字典类型
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public DictTypeResponse update(String id, DictTypeUpdateRequest req) {
        DictTypeEntity e = dictTypeMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        QueryWrapper qw = QueryWrapper.create()
                .where("dict_type = ?", req.getDictType())
                .and("id != ?", id);
        if (dictTypeMapper.selectCountByQuery(qw) > 0) {
            throw new BizException("字典类型已存在");
        }
        e.setDictName(req.getDictName());
        e.setDictType(req.getDictType());
        e.setStatus(req.getStatus());
        e.setRemark(req.getRemark());
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        dictTypeMapper.update(e);
        return toResponse(e);
    }

    /**
     * 删除字典类型
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public boolean delete(String id) {
        DictTypeEntity e = dictTypeMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return dictTypeMapper.update(e) > 0;
    }

    /**
     * 获取字典类型详情
     */
    public DictTypeResponse get(String id) {
        DictTypeEntity e = dictTypeMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        return toResponse(e);
    }

    /**
     * 分页查询字典类型
     */
    public PageResponse<DictTypeResponse> list(DictTypeQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");
        
        if (req.getDictName() != null && !req.getDictName().isBlank()) {
            qw.and("dict_name LIKE ?", "%" + req.getDictName() + "%");
        }
        if (req.getDictType() != null && !req.getDictType().isBlank()) {
            qw.and("dict_type LIKE ?", "%" + req.getDictType() + "%");
        }
        if (req.getStatus() != null) {
            qw.and("status = ?", req.getStatus());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(dict_name LIKE ? OR dict_type LIKE ?)", kw, kw);
        }

        return PageHelper.apply(dictTypeMapper, qw, req, this::toResponse);
    }

    private DictTypeResponse toResponse(DictTypeEntity e) {
        DictTypeResponse r = new DictTypeResponse();
        BeanMapper.mapper(e, r);
        return r;
    }
}
