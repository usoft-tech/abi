package com.usoft.framework.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.entity.DepartmentEntity;

/**
 * 部门Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<DepartmentEntity> {

    /**
     * 按租户查询全部部门
     */
    default List<DepartmentEntity> listByTenant(String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectListByQuery(qw);
    }
}
