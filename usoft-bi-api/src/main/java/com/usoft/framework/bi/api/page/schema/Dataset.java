package com.usoft.framework.bi.api.page.schema;

import java.util.List;

import com.usoft.framework.bi.api.dataset.schema.DataSetConfig.Output;

import lombok.Data;

@Data
public class Dataset {
    /**
     * 数据集ID
     */
    private String id;
    /**
     * 数据集名称
     */
    private String name;
    /**
     * 数据集描述
     */
    private Output output;

    /**
     * 数据集键值（变量名）
     */
    private String key;

    /**
     * 数据集依赖（variables）
     */
    private List<String> dependencies;

    /**
     * 数据集AI提示
     */
    private String aiPrompt;
}
