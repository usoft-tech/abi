package com.usoft.framework.security.thirdauth.provider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.security.thirdauth.config.ThirdAuthProperties;
import com.usoft.framework.security.thirdauth.model.ThirdAuthCallback;
import com.usoft.framework.security.thirdauth.model.ThirdAuthToken;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feishu authentication provider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeishuProvider implements ThirdAuthProvider {

    private final ThirdAuthProperties properties;
    private final HttpClient httpClient;

    @Override
    public String getPlatform() {
        return "feishu";
    }

    @Override
    public String getAuthorizeUrl(String state) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("Feishu configuration not found");
        }
        return String.format("https://passport.feishu.cn/suite/passport/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&state=%s",
                config.getClientId(), config.getRedirectUri(), state);
    }

    @Override
    public ThirdAuthUser login(ThirdAuthCallback callback) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("Feishu configuration not found");
        }

        // 1. Get Access Token
        String tokenUrl = "https://passport.feishu.cn/suite/passport/oauth/token";
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "authorization_code");
        formData.put("client_id", config.getClientId());
        formData.put("client_secret", config.getClientSecret());
        formData.put("code", callback.getCode());
        formData.put("redirect_uri", config.getRedirectUri());

        String formBody = formData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        String accessToken;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get Feishu access token: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            accessToken = json.getString("access_token");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request Feishu access token", e);
        }

        // 2. Get User Info
        String userUrl = "https://passport.feishu.cn/suite/passport/oauth/userinfo";
        HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(userUrl))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get Feishu user info: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);

            ThirdAuthToken token = ThirdAuthToken.builder()
                    .accessToken(accessToken)
                    .build();

            return ThirdAuthUser.builder()
                    .uuid(json.getString("open_id")) // or union_id
                    .username(json.getString("name"))
                    .nickname(json.getString("name"))
                    .avatar(json.getString("avatar_url"))
                    .token(token)
                    .source(getPlatform())
                    .rawUserInfo(json)
                    .build();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request Feishu user info", e);
        }
    }
}
