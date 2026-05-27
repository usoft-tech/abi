package com.usoft.framework.bi.api.page.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ApiDataSource.class, name = "api"),
    @JsonSubTypes.Type(value = SqlDataSource.class, name = "sql")
})
public abstract class DataSourceItem {
    private String id;
    private String type;
    private String name;
}
