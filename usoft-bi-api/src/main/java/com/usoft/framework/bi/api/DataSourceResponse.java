package com.usoft.framework.bi.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DataSourceResponse {
    private String id;
    private String name;
    private String type;
    private String driverClassName;
    private String url;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    @JsonIgnore
    private String password;
    private String extProps;

}

