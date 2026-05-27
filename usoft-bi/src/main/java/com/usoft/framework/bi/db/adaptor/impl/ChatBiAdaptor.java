package com.usoft.framework.bi.db.adaptor.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.usoft.framework.bi.api.DataSourceCreateRequest;
import com.usoft.framework.bi.api.db.schema.DatabaseSchema;
import com.usoft.framework.bi.entity.DataSourceEntity;
import com.usoft.framework.bi.mapper.DataSourceMapper;
import com.usoft.framework.utils.SpringUtils;

import lombok.extern.slf4j.Slf4j;

import com.usoft.framework.utils.ObjectMapperUtils;

@Slf4j
public class ChatBiAdaptor extends BaseAdaptor {

    private static final String TEST_CONNECTION_URL = "/api/auth/user/getCurrentUser";

    private static final String SCHEMA_URL = "/api/chat/agent/agentListInfo";

    private final DataSourceEntity entity;

    public ChatBiAdaptor(String id) {
        super(id);
        entity = SpringUtils.getBean(DataSourceMapper.class).selectOneById(id);
    }

    @Override
    public boolean testConnection(DataSourceCreateRequest req) {

        String url = req.getUrl();
        String authorization = req.getPassword();
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url + TEST_CONNECTION_URL))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            if (authorization != null && !authorization.isBlank()) {
                requestBuilder.header("Authorization", authorization);
            }
            HttpResponse<String> resp = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<DatabaseSchema> getDatabaseSchemas() {
        if (entity == null) {
            return List.of();
        }
        String url = entity.getUrl();
        String authorization = entity.getPassword();
        if (url == null || url.isBlank()) {
            return List.of();
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url + SCHEMA_URL))
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString("{}", StandardCharsets.UTF_8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            if (authorization != null && !authorization.isBlank()) {
                requestBuilder.header("Authorization", authorization);
            }
            HttpResponse<String> resp = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return List.of();
            }
            String body = resp.body();
            if (body == null || body.isBlank()) {
                return List.of();
            }
            ChatBiResponse<List<ChatBiApp>> parsed = ObjectMapperUtils.fromJson(body,
                    new TypeReference<ChatBiResponse<List<ChatBiApp>>>() {
                    });
            if (parsed == null || parsed.code == null || parsed.code != 200 || parsed.data == null) {
                return List.of();
            }
            return mapToSchemas(parsed.data);
        } catch (Exception e) {
            log.error("Failed to get database schemas from ChatBI", e);
            return List.of();
        }
    }

    private static List<DatabaseSchema> mapToSchemas(List<ChatBiApp> apps) {
        if (apps == null || apps.isEmpty()) {
            return List.of();
        }
        List<DatabaseSchema> result = new ArrayList<>();
        for (ChatBiApp app : apps) {
            if (app == null) {
                continue;
            }
            DatabaseSchema schema = new DatabaseSchema();
            schema.setDbid(String.valueOf(app.id));
            schema.setName(app.name);
            schema.setType("chatbi");
            schema.setDescription(app.description);
            schema.setTables(mapTables(app.dataSetSchemas));
            result.add(schema);
        }
        return result;
    }

    private static DatabaseSchema.Table[] mapTables(List<ChatBiDataSetSchema> dataSetSchemas) {
        if (dataSetSchemas == null || dataSetSchemas.isEmpty()) {
            return new DatabaseSchema.Table[0];
        }
        List<DatabaseSchema.Table> tables = new ArrayList<>();
        for (ChatBiDataSetSchema ds : dataSetSchemas) {
            if (ds == null) {
                continue;
            }
            DatabaseSchema.Table table = new DatabaseSchema.Table();
            table.setName(ds.name);
            table.setDescription(ds.description);
            table.setColumns(mapColumns(ds.metrics, ds.dimensions));
            tables.add(table);
        }
        return tables.toArray(DatabaseSchema.Table[]::new);
    }

    private static DatabaseSchema.Column[] mapColumns(List<String> metrics, List<String> dimensions) {
        List<DatabaseSchema.Column> cols = new ArrayList<>();
        if (metrics != null) {
            for (String m : metrics) {
                if (m == null || m.isBlank()) {
                    continue;
                }
                DatabaseSchema.Column c = new DatabaseSchema.Column();
                c.setName(m);
                c.setType("metric");
                c.setDescription(null);
                cols.add(c);
            }
        }
        if (dimensions != null) {
            for (String d : dimensions) {
                if (d == null || d.isBlank()) {
                    continue;
                }
                DatabaseSchema.Column c = new DatabaseSchema.Column();
                c.setName(d);
                c.setType("dimension");
                c.setDescription(null);
                cols.add(c);
            }
        }
        return cols.toArray(DatabaseSchema.Column[]::new);
    }

    private static class ChatBiResponse<T> {
        public Integer code;
        public String msg;
        public T data;
        public Long timestamp;
        public String traceId;
    }

    private static class ChatBiApp {
        public Long id;
        public String name;
        public String description;
        public List<ChatBiDataSetSchema> dataSetSchemas;
    }

    private static class ChatBiDataSetSchema {
        public String name;
        public String description;
        public List<String> metrics;
        public List<String> dimensions;
    }
}
