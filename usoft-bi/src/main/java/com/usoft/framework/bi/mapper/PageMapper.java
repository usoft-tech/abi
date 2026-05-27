package com.usoft.framework.bi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.entity.PageEntity;

@Mapper
public interface PageMapper extends BaseMapper<PageEntity> {
    default PageEntity findByIdInTenant(String id, String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("id = ?", id)
                .and("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectOneByQuery(qw);
    }

    default List<PageEntity> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}

