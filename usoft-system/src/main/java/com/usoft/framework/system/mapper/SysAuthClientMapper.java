package com.usoft.framework.system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.system.entity.SysAuthClientEntity;

/**
 * 客户端凭证数据访问
 */
@Mapper
public interface SysAuthClientMapper extends BaseMapper<SysAuthClientEntity> {

    /**
     * 查询当前租户下的客户端凭证
     */
    default SysAuthClientEntity selectCurrentTenantClient(String tenantId, String clientId) {
        QueryWrapper qw = QueryWrapper.create()
                .where("tenant_id = ?", tenantId)
                .and("client_id = ?", clientId);
        return selectOneByQuery(qw);
    }
}
