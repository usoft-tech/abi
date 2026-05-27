package com.usoft.framework.ai.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.ai.entity.ModelEntity;

/**
 * 模型Mapper
 */
@Mapper
public interface ModelMapper extends BaseMapper<ModelEntity> {
    default ModelEntity findByIdInTenant(String id, String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("id = ?", id)
                .and("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectOneByQuery(qw);
    }

    default List<ModelEntity> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}

