package com.usoft.framework.bi.api;

import lombok.Data;

@Data
public class DataSourceUpdateRequest {
    private String name;
    private String type;
    private String driverClassName;
    private String url;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String extProps;

}

