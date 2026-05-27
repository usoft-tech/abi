package com.usoft.framework.bi.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.usoft.framework.bi.entity.DatasourceDbEntity;

@Mapper
public interface DatasourceDbMapper extends BaseMapper<DatasourceDbEntity> {
}

