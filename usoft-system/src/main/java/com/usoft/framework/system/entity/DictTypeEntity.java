package com.usoft.framework.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.usoft.framework.common.enums.EnableStatus;
import lombok.Data;
import java.time.Instant;

/**
 * 字典类型实体
 */
@Data
@Table("sys_dict_type")
public class DictTypeEntity {
    /**
     * 主键 ID
     */
    @Id
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
     * 是否删除
     */
    private Boolean isDeleted;

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
