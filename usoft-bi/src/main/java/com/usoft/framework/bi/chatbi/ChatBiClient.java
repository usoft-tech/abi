package com.usoft.framework.bi.chatbi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.usoft.framework.utils.ObjectMapperUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatBiClient {

    public static String generateSql(Integer agentId, String url, String token, String prompt) {
        try {
            Request request = Request.builder()
                    .agentId(agentId)
                    .chatId(0)
                    .queryText(prompt)
                    .chatShow(false)
                    .queryFilters(QueryFilters.builder().filters(List.of()).build())
                    .build();

            String jsonBody = ObjectMapperUtils.toJson(request);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/chat/query/parse"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (token != null && !token.isEmpty()) {
                reqBuilder.header("Authorization", token);
            }

            HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed with status code: " + response.statusCode());
            }

            Response biResponse = ObjectMapperUtils.fromJson(response.body(), Response.class);
            if (biResponse == null || biResponse.getData() == null) {
                return null;
            }

            if (biResponse.getCode() != null && biResponse.getCode() != 200) {
                throw new RuntimeException("API error: " + biResponse.getMsg());
            }

            if (biResponse.getData().getState() != null && !biResponse.getData().getState().equals("COMPLETED")) {
                throw new RuntimeException("API error: " + biResponse.getData().getErrorMsg());
            }

            List<SelectedParse> selectedParses = biResponse.getData().getSelectedParses();
            if (selectedParses != null && !selectedParses.isEmpty()) {
                SelectedParse parse = selectedParses.get(0);
                if (parse.getSqlInfo() != null) {
                    return parse.getSqlInfo().getQuerySQL();
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SQL", e);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Request {
        private String queryText;
        private Integer chatId;
        private Integer agentId;
        private QueryFilters queryFilters;
        private Boolean chatShow;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class QueryFilters {
        private List<Object> filters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Response {
        private Integer code;
        private String msg;
        private ChatBiData data;
        private Long timestamp;
        private String traceId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatBiData {
        private Integer queryId;
        private String state;
        private String errorMsg;
        private List<SelectedParse> selectedParses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SelectedParse {
        private Integer id;
        private String queryMode;
        private String queryType;
        private String filterType;
        private String aggType;
        private Integer limit;
        private Integer score;
        private Object dateInfo;
        private SqlInfo sqlInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SqlInfo {
        private String parsedS2SQL;
        private String correctedS2SQL;
        private String querySQL;
        private String correctedQuerySQL;
    }
}
