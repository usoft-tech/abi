package com.usoft.framework.bi.api.page.schema;

import lombok.Data;

@Data
public class SchemaItemDatasource {
    private String source;
    private String datasourceId;
    private String scriptId;
    private String custom;
    private Dataset dataset;
}
