package com.usoft.framework.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.entity.TenantEntity;

/**
 * 租户Mapper
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantEntity> {

    default List<TenantEntity> listAll() {
        QueryWrapper qw = QueryWrapper.create()
                .where("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}
