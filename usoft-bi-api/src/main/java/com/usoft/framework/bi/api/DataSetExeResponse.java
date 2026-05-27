package com.usoft.framework.bi.api;

import lombok.Data;

@Data
public class DataSetExeResponse {

    /**
     * 数据集执行结果
     */
    private Object result;
    /**
     * 数据集执行结果解释
     */
    private String explain;
}
