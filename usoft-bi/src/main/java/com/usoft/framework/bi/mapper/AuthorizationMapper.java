package com.usoft.framework.bi.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.usoft.framework.bi.entity.AuthorizationEntity;

/**
 * 授权Mapper
 */
@Mapper
public interface AuthorizationMapper extends BaseMapper<AuthorizationEntity> {
}
