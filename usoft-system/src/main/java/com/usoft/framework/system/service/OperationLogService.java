package com.usoft.framework.system.service;

import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryChain;
import com.usoft.framework.system.entity.OperationLogEntity;
import com.usoft.framework.system.mapper.OperationLogMapper;

/**
 * 操作日志服务
 */
@Service
public class OperationLogService {
    private final OperationLogMapper logMapper;

    /**
     * 构造日志服务
     */
    public OperationLogService(OperationLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    /**
     * 保存操作日志
     */
    public void save(OperationLogEntity log) {
        logMapper.insert(log);
    }

    /**
     * 统计日志数量
     */
    public long count() {
        return QueryChain.of(logMapper).count();
    }
}
