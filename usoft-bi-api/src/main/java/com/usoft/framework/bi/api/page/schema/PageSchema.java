package com.usoft.framework.bi.api.page.schema;

import lombok.Data;
import java.util.List;

@Data
public class PageSchema {
    private PageInfo info;
    private List<DataSourceItem> datasources;
    private List<ScriptItem> scripts;
    private List<VariableItem> variables;
    private List<SchemaItem> items;
    private List<Dataset> datasets;
}
