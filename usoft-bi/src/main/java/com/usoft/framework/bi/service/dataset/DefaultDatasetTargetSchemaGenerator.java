package com.usoft.framework.bi.service.dataset;

import com.usoft.framework.bi.api.page.schema.SchemaItem;
import com.usoft.framework.utils.ObjectMapperUtils;

public class DefaultDatasetTargetSchemaGenerator implements DatasetTargetSchemaGenerator {

    @Override
    public boolean support(SchemaItem schemaItem) {
        return true;
    }

    @Override
    public String generate(SchemaItem schemaItem) {
        return formatted(ObjectMapperUtils.toJson(schemaItem));
    }

    protected String formatted(String template) {
        return "<data_target_schema description=\"最终结果数据结构必须从该JSON数据中挖掘数据需求\">%s</data_target_schema>".formatted(template);
    }
}
