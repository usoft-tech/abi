package com.usoft.framework.bi.api.page.schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SqlDataSource extends DataSourceItem {
    private String query;
}
