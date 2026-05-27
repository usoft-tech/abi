package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.enums.EnableStatus;
import lombok.Data;

/**
 * 字典类型查询请求
 */
@Data
public class DictTypeQueryRequest extends PageRequest {
    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（ENABLE正常 DISABLE停用）
     */
    private EnableStatus status;

    /**
     * 关键字（模糊查询字典名称或类型）
     */
    private String keyword;
}
