package com.usoft.framework.security.thirdauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for third-party authentication.
 */
@Data
@Component
@ConfigurationProperties(prefix = "usoft.security.third-auth")
public class ThirdAuthProperties {

    /**
     * Map of platform configurations.
     * Key is the platform name (e.g., github, wechat).
     */
    private Map<String, ThirdAuthClientConfig> clients = new HashMap<>();

    /**
     * Frontend URL to redirect after login.
     */
    private String frontendUrl = "http://localhost:5173/third-auth/callback";

    @Data
    public static class ThirdAuthClientConfig {
        /**
         * Client ID (App Key).
         */
        private String clientId;

        /**
         * Client Secret (App Secret).
         */
        private String clientSecret;

        /**
         * Redirect URI.
         */
        private String redirectUri;

        /**
         * Agent ID (for platforms like DingTalk).
         */
        private String agentId;
        
        /**
         * Stack Overflow Key (if needed).
         */
        private String stackOverflowKey;

        /**
         * Union ID (if needed).
         */
        private boolean unionId;
    }
}
