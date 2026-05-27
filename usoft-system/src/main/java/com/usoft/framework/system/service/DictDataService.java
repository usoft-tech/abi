package com.usoft.framework.system.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.api.PageResponse;
import com.usoft.framework.common.bean.BeanMapper;
import com.usoft.framework.common.enums.EnableStatus;
import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.core.mybatis.PageHelper;
import com.usoft.framework.core.security.UserHolder;
import com.usoft.framework.system.api.DictDataCreateRequest;
import com.usoft.framework.system.api.DictDataQueryRequest;
import com.usoft.framework.system.api.DictDataResponse;
import com.usoft.framework.system.api.DictDataUpdateRequest;
import com.usoft.framework.system.entity.DictDataEntity;
import com.usoft.framework.system.mapper.DictDataMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 字典数据服务
 */
@Service
public class DictDataService {
    private final DictDataMapper dictDataMapper;

    public DictDataService(DictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    /**
     * 创建字典数据
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public DictDataResponse create(DictDataCreateRequest req) {
        QueryWrapper valueQw = QueryWrapper.create()
                .where("dict_type = ?", req.getDictType())
                .and("dict_value = ?", req.getDictValue());
        if (dictDataMapper.selectCountByQuery(valueQw) > 0) {
            throw new BizException("字典键值已存在");
        }

        QueryWrapper labelQw = QueryWrapper.create()
                .where("dict_type = ?", req.getDictType())
                .and("dict_label = ?", req.getDictLabel());
        if (dictDataMapper.selectCountByQuery(labelQw) > 0) {
            throw new BizException("字典标签已存在");
        }

        DictDataEntity e = new DictDataEntity();
        e.setId(UUID.randomUUID().toString());
        e.setDictSort(req.getDictSort() != null ? req.getDictSort() : 0);
        e.setDictLabel(req.getDictLabel());
        e.setDictValue(req.getDictValue());
        e.setDictType(req.getDictType());
        e.setCssClass(req.getCssClass());
        e.setListClass(req.getListClass());
        e.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : "N");
        e.setStatus(req.getStatus() != null ? req.getStatus() : EnableStatus.ENABLE);
        e.setRemark(req.getRemark());
        e.setIsDeleted(false);
        e.setCreatedAt(Instant.now());
        e.setCreatedBy(UserHolder.username());
        dictDataMapper.insert(e);
        return toResponse(e);
    }

    /**
     * 更新字典数据
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public DictDataResponse update(String id, DictDataUpdateRequest req) {
        DictDataEntity e = dictDataMapper.selectOneById(id);
        if (e == null) {
            return null;
        }

        QueryWrapper valueQw = QueryWrapper.create()
                .where("dict_type = ?", req.getDictType())
                .and("dict_value = ?", req.getDictValue())
                .and("id != ?", id);
        if (dictDataMapper.selectCountByQuery(valueQw) > 0) {
            throw new BizException("字典键值已存在");
        }

        QueryWrapper labelQw = QueryWrapper.create()
                .where("dict_type = ?", req.getDictType())
                .and("dict_label = ?", req.getDictLabel())
                .and("id != ?", id);
        if (dictDataMapper.selectCountByQuery(labelQw) > 0) {
            throw new BizException("字典标签已存在");
        }

        e.setDictSort(req.getDictSort());
        e.setDictLabel(req.getDictLabel());
        e.setDictValue(req.getDictValue());
        e.setDictType(req.getDictType());
        e.setCssClass(req.getCssClass());
        e.setListClass(req.getListClass());
        e.setIsDefault(req.getIsDefault());
        e.setStatus(req.getStatus());
        e.setRemark(req.getRemark());
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        dictDataMapper.update(e);
        return toResponse(e);
    }

    /**
     * 删除字典数据
     */
    @CacheEvict(value = "sysDict", allEntries = true)
    public boolean delete(String id) {
        DictDataEntity e = dictDataMapper.selectOneById(id);
        if (e == null) {
            return false;
        }
        e.setIsDeleted(true);
        e.setUpdatedAt(Instant.now());
        e.setUpdatedBy(UserHolder.username());
        return dictDataMapper.update(e) > 0;
    }

    /**
     * 获取字典数据详情
     */
    public DictDataResponse get(String id) {
        DictDataEntity e = dictDataMapper.selectOneById(id);
        if (e == null) {
            return null;
        }
        return toResponse(e);
    }

    /**
     * 分页查询字典数据
     */
    public PageResponse<DictDataResponse> list(DictDataQueryRequest req) {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");

        if (req.getDictType() != null && !req.getDictType().isBlank()) {
            qw.and("dict_type = ?", req.getDictType());
        }
        if (req.getDictLabel() != null && !req.getDictLabel().isBlank()) {
            qw.and("dict_label LIKE ?", "%" + req.getDictLabel() + "%");
        }
        if (req.getStatus() != null) {
            qw.and("status = ?", req.getStatus());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword() + "%";
            qw.and("(dict_label LIKE ? OR dict_value LIKE ?)", kw, kw);
        }
        
        qw.orderBy("dict_sort", true);

        return PageHelper.apply(dictDataMapper, qw, req, this::toResponse);
    }

    /**
     * 根据字典类型获取字典数据列表
     */
    @Cacheable(value = "sysDict", key = "#dictType")
    public List<DictDataResponse> listByType(String dictType) {
        QueryWrapper qw = QueryWrapper.create()
                .where("dict_type = ?", dictType)
                .and("(is_deleted = 0 OR is_deleted IS NULL)")
                .and("status = ?", EnableStatus.ENABLE)
                .orderBy("dict_sort", true);
        
        List<DictDataEntity> entities = dictDataMapper.selectListByQuery(qw);
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private DictDataResponse toResponse(DictDataEntity e) {
        DictDataResponse r = new DictDataResponse();
        BeanMapper.mapper(e, r);
        return r;
    }
}
