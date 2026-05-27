package com.usoft.framework.bi.service.dataset;

import java.util.Objects;

import com.usoft.framework.bi.api.page.schema.SchemaItem;
import com.usoft.framework.bi.api.page.schema.SchemaItemDatasource;

public class EchartsDatasetTargetSchemaGenerator extends DefaultDatasetTargetSchemaGenerator {

    @Override
    public boolean support(SchemaItem schemaItem) {
        return "b-echarts".equals(schemaItem.getType());
    }

    @Override
    public String generate(SchemaItem schemaItem) {
        SchemaItemDatasource datasource = schemaItem.getDatasource();
        if ("custom".equals(datasource.getSource()) && datasource.getCustom() != null
                && !datasource.getCustom().isEmpty()) {

            String template = "<data_target_schema type=\"example\" description=\"最终结果数据结构必须与该示例数据结构一致\"><example>%s</example></data_target_schema>";
            return template.formatted(datasource.getCustom());
        }
        String template = "<data_target_schema type=\"echarts\" description=\"最终结果数据结构必须符合该Echarts数据脚本中series.data格式\"><script>%s</script></data_target_schema>";
        return template.formatted((String) Objects.requireNonNullElse(schemaItem.getProps().get("script"), ""));
    }
}
