package com.usoft.framework.bi.service.dataset;

import com.usoft.framework.bi.api.page.schema.SchemaItem;

public class HtmlDatasetTargetSchemaGenerator implements DatasetTargetSchemaGenerator {

    @Override
    public boolean support(SchemaItem schemaItem) {
        return "b-html".equals(schemaItem.getType());
    }

    @Override
    public String generate(SchemaItem schemaItem) {
        String template = (String) schemaItem.getProps().getOrDefault("template", "无");
        return "<data_target_schema type=\"html\" description=\"最终结果数据结构必须从该HTML数据中挖掘数据需求\"><var-formatter>{{var}}</var-formatter><template>%s</template><example>{\"name\":\"张三\",\"age\":18}</example></data_target_schema>".formatted(template);
    }

}
