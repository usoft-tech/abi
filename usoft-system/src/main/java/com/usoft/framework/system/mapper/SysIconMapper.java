package com.usoft.framework.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.entity.SysIconEntity;

/**
 * 系统图标Mapper
 */
@Mapper
public interface SysIconMapper extends BaseMapper<SysIconEntity> {

    /**
     * 按租户查询全部图标
     */
    default List<SysIconEntity> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}

