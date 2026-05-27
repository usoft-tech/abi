package com.usoft.framework.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import com.usoft.framework.system.entity.OperationLogEntity;

/**
 * 操作日志Mapper接口
 * 用于持久化操作日志实体，提供基础的CRUD能力
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
