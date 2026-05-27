package com.usoft.framework.bi.service.dataset;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.usoft.framework.bi.api.page.schema.SchemaItem;

public class DatasetTargetSchemaGeneratorProxy {

    private static List<DatasetTargetSchemaGenerator> generators = new LinkedList<>();

    static {
        generators.add(new HtmlDatasetTargetSchemaGenerator());
        generators.add(new EchartsDatasetTargetSchemaGenerator());
        generators.add(new DefaultDatasetTargetSchemaGenerator());
    }

    public static String generate(SchemaItem schemaItem) {
        for (DatasetTargetSchemaGenerator generator : generators) {
            if (generator.support(schemaItem)) {
                return Objects.requireNonNullElse(generator.generate(schemaItem), "");
            }
        }
        return "";
    }
}
