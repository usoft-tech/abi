package com.usoft.framework.bi.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.bi.entity.PageReportEntity;

@Mapper
public interface PageReportMapper extends BaseMapper<PageReportEntity> {
    
    /**
     * 根据ID和租户ID查询
     * @param id ID
     * @param tenantId 租户ID
     * @return 实体
     */
    default PageReportEntity findByIdInTenant(String id, String tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("id = ?", id)
                .and("tenant_id = ?", tenantId)
                .and("(is_deleted = 0 OR is_deleted IS NULL)");
        return selectOneByQuery(qw);
    }
}
