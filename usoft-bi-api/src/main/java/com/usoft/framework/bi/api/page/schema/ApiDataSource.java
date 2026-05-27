package com.usoft.framework.bi.api.page.schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiDataSource extends DataSourceItem {
    private String url;
    private String method;
    private List<NameValue> headers;
    private List<NameValue> formData;
    private String body;
}
