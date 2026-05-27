package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典数据创建请求
 */
@Data
public class DictDataCreateRequest {
    /**
     * 字典排序
     */
    private Integer dictSort;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /**
     * 字典键值
     */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
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
}
