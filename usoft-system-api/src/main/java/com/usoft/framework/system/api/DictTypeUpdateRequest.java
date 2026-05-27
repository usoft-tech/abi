package com.usoft.framework.system.api;

import com.usoft.framework.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典类型更新请求
 */
@Data
public class DictTypeUpdateRequest {
    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /**
     * 状态（ENABLE正常 DISABLE停用）
     */
    private EnableStatus status;

    /**
     * 备注
     */
    private String remark;
}
