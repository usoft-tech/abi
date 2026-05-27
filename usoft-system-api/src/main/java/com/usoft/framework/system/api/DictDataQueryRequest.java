package com.usoft.framework.system.api;

import com.usoft.framework.common.api.PageRequest;
import com.usoft.framework.common.enums.EnableStatus;
import lombok.Data;

/**
 * 字典数据查询请求
 */
@Data
public class DictDataQueryRequest extends PageRequest {
    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 状态（ENABLE正常 DISABLE停用）
     */
    private EnableStatus status;

    /**
     * 关键字（模糊查询字典标签 or 键值）
     */
    private String keyword;
}
