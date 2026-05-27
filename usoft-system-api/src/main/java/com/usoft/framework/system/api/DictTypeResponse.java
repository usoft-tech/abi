package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;
import lombok.Data;
import java.time.Instant;

/**
 * 字典类型响应体
 */
@Data
public class DictTypeResponse {
    /**
     * 主键 ID
     */
    private String id;

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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 更新人
     */
    private String updatedBy;
}
