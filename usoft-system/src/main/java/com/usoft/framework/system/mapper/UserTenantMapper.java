package com.usoft.framework.system.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.usoft.framework.system.entity.UserTenantEntity;

/**
 * 用户租户Mapper
 */
@Mapper
public interface UserTenantMapper extends BaseMapper<UserTenantEntity> {
}
