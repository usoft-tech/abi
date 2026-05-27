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
 * Douyin authentication provider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DouyinProvider implements ThirdAuthProvider {

    private final ThirdAuthProperties properties;
    private final HttpClient httpClient;

    @Override
    public String getPlatform() {
        return "douyin";
    }

    @Override
    public String getAuthorizeUrl(String state) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("Douyin configuration not found");
        }
        return String.format("https://open.douyin.com/platform/oauth/connect?client_key=%s&response_type=code&scope=user_info&redirect_uri=%s&state=%s",
                config.getClientId(), config.getRedirectUri(), state);
    }

    @Override
    public ThirdAuthUser login(ThirdAuthCallback callback) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("Douyin configuration not found");
        }

        // 1. Get Access Token
        String tokenUrl = "https://open.douyin.com/oauth/access_token/";
        Map<String, String> formData = new HashMap<>();
        formData.put("client_key", config.getClientId());
        formData.put("client_secret", config.getClientSecret());
        formData.put("code", callback.getCode());
        formData.put("grant_type", "authorization_code");

        String formBody = formData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        String accessToken;
        String openid;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get Douyin access token: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            if (json.containsKey("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.containsKey("error_code") && data.getIntValue("error_code") != 0) {
                     throw new BizException("Douyin error: " + data.getString("description"));
                }
                accessToken = data.getString("access_token");
                openid = data.getString("open_id");
            } else {
                throw new BizException("Douyin access token response format error: " + body);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request Douyin access token", e);
        }

        // 2. Get User Info
        String userUrl = "https://open.douyin.com/oauth/userinfo/";
        Map<String, String> userFormData = new HashMap<>();
        userFormData.put("access_token", accessToken);
        userFormData.put("open_id", openid);

        String userFormBody = userFormData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(userUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(userFormBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get Douyin user info: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                throw new BizException("Douyin user info response format error: " + body);
            }
            if (data.containsKey("error_code") && data.getIntValue("error_code") != 0) {
                throw new BizException("Douyin user info error: " + data.getString("description"));
            }

            ThirdAuthToken token = ThirdAuthToken.builder()
                    .accessToken(accessToken)
                    .openId(openid)
                    .build();

            return ThirdAuthUser.builder()
                    .uuid(openid)
                    .username(data.getString("nickname"))
                    .nickname(data.getString("nickname"))
                    .avatar(data.getString("avatar"))
                    .token(token)
                    .source(getPlatform())
                    .rawUserInfo(json)
                    .build();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request Douyin user info", e);
        }
    }
}
