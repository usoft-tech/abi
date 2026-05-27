package com.usoft.framework.bi.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestSqlRequest {

    /**
     * 数据库ID
     */
    @NotBlank(message = "数据库不能为空")
    private String dbId;
    /**
     * SQL语句
     */
    @NotBlank(message = "SQL语句不能为空")
    private String sql;
}
