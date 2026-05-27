package com.usoft.framework.bi.service.dataset;

import com.usoft.framework.bi.api.page.schema.SchemaItem;

public interface DatasetTargetSchemaGenerator {

    boolean support(SchemaItem schemaItem);

    String generate(SchemaItem schemaItem);
}
