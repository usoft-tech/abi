package com.usoft.framework.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.entity.SysConfigEntity;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigEntity> {

    /**
     * 按租户查询全部配置
     */
    default List<SysConfigEntity> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}
