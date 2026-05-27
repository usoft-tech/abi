package com.usoft.framework.bi.api;

import com.usoft.framework.bi.api.dataset.schema.DataSetConfig.Output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateSqlResponse {

    private String sql;
    private String script;
    private Output output;
}
