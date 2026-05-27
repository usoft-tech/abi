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

/**
 * WeChat Open Platform authentication provider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatOpenProvider implements ThirdAuthProvider {

    private final ThirdAuthProperties properties;
    private final HttpClient httpClient;

    @Override
    public String getPlatform() {
        return "wechat_open";
    }

    @Override
    public String getAuthorizeUrl(String state) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("WeChat configuration not found");
        }
        return String.format("https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
                config.getClientId(), config.getRedirectUri(), state);
    }

    @Override
    public ThirdAuthUser login(ThirdAuthCallback callback) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("WeChat configuration not found");
        }

        // 1. Get Access Token
        String tokenUrl = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                config.getClientId(), config.getClientSecret(), callback.getCode());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .GET()
                .build();

        String accessToken;
        String openid;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get WeChat access token: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                throw new BizException("WeChat error: " + json.getString("errmsg"));
            }
            accessToken = json.getString("access_token");
            openid = json.getString("openid");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request WeChat access token", e);
        }

        // 2. Get User Info
        String userUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                accessToken, openid);
        HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(userUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get WeChat user info: " + response.statusCode());
            }
            String body = response.body();
            JSONObject json = JSON.parseObject(body);
            if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                throw new BizException("WeChat user info error: " + json.getString("errmsg"));
            }

            ThirdAuthToken token = ThirdAuthToken.builder()
                    .accessToken(accessToken)
                    .openId(openid)
                    .unionId(json.getString("unionid"))
                    .build();

            return ThirdAuthUser.builder()
                    .uuid(openid)
                    .username(json.getString("nickname"))
                    .nickname(json.getString("nickname"))
                    .avatar(json.getString("headimgurl"))
                    .token(token)
                    .source(getPlatform())
                    .rawUserInfo(json)
                    .build();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request WeChat user info", e);
        }
    }
}
