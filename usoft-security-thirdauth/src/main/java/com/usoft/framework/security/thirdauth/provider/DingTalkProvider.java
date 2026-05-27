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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.URLEncoder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * DingTalk authentication provider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkProvider implements ThirdAuthProvider {

    private final ThirdAuthProperties properties;
    private final HttpClient httpClient;

    @Override
    public String getPlatform() {
        return "dingtalk";
    }

    @Override
    public String getAuthorizeUrl(String state) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("DingTalk configuration not found");
        }
        return String.format("https://oapi.dingtalk.com/connect/qrconnect?appid=%s&response_type=code&scope=snsapi_login&state=%s&redirect_uri=%s",
                config.getClientId(), state, config.getRedirectUri());
    }

    @Override
    public ThirdAuthUser login(ThirdAuthCallback callback) {
        ThirdAuthProperties.ThirdAuthClientConfig config = properties.getClients().get(getPlatform());
        if (config == null) {
            throw new BizException("DingTalk configuration not found");
        }

        long timestamp = System.currentTimeMillis();
        String signature = sign(timestamp, config.getClientSecret());
        
        // Note: The URL parameter 'signature' must be URL-encoded. The sign() method returns it encoded.
        // However, we should be careful about double encoding if we construct URL manually or via URI.
        // URI.create() expects a valid URI string.
        // The signature from sign() is already URL encoded.
        
        String url = String.format("https://oapi.dingtalk.com/sns/getuserinfo_bycode?accessKey=%s&timestamp=%s&signature=%s",
                config.getClientId(), timestamp, signature);

        JSONObject requestBody = new JSONObject();
        requestBody.put("tmp_auth_code", callback.getCode());
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJSONString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BizException("Failed to get DingTalk user info: " + response.statusCode());
            }
            String respBody = response.body();
            JSONObject json = JSON.parseObject(respBody);
            if (json.getIntValue("errcode") != 0) {
                throw new BizException("DingTalk error: " + json.getString("errmsg"));
            }
            
            JSONObject userInfo = json.getJSONObject("user_info");
            
            ThirdAuthToken token = ThirdAuthToken.builder()
                    // DingTalk v1 doesn't return access token here, it just gives user info directly
                    .openId(userInfo.getString("openid"))
                    .unionId(userInfo.getString("unionid"))
                    .build();

            return ThirdAuthUser.builder()
                    .uuid(userInfo.getString("openid"))
                    .username(userInfo.getString("nick"))
                    .nickname(userInfo.getString("nick"))
                    // .avatar(userInfo.getString("avatar")) // v1 might not return avatar in this call
                    .token(token)
                    .source(getPlatform())
                    .rawUserInfo(json)
                    .build();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to request DingTalk user info", e);
        }
    }
    
    private String sign(long timestamp, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] signatureBytes = mac.doFinal(String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            return URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException("Failed to sign DingTalk request", e);
        }
    }
}
