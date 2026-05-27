package com.usoft.framework.bi.api;

import lombok.Data;

@Data
public class GenerateSqlRequest {

    private String prompt;
    private String datasourceId;
    private Integer agentId;
}
