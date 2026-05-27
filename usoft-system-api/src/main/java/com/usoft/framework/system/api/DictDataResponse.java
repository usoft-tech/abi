package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;
import lombok.Data;
import java.time.Instant;

/**
 * 字典数据响应体
 */
@Data
public class DictDataResponse {
    /**
     * 主键 ID
     */
    private String id;

    /**
     * 字典排序
     */
    private Integer dictSort;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    private String isDefault;

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
