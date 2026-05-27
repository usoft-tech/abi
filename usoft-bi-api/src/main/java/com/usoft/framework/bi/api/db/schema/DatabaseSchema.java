package com.usoft.framework.bi.api.db.schema;

import lombok.Data;

@Data
public class DatabaseSchema {

    private String name;
    private String type;
    private String dbid;
    private String description;

    private Table[] tables;

    @Data
    public static class Table {
        private String name;
        private String description;
        private Column[] columns;
    }

    @Data
    public static class Column {
        private String name;
        private String type;
        private String description;
    }
}
