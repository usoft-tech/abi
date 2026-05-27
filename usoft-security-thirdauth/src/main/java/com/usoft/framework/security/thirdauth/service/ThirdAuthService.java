package com.usoft.framework.security.thirdauth.service;

import com.usoft.framework.security.thirdauth.model.ThirdAuthCallback;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;

/**
 * Service for handling third-party authentication.
 */
public interface ThirdAuthService {

    /**
     * Get the authorization URL for the specified platform.
     *
     * @param platform The platform name (e.g., "wechat_open", "feishu", "github", "douyin", "dingtalk")
     * @return The authorization URL.
     */
    String getAuthorizeUrl(String platform);

    /**
     * Login with the callback from the third-party platform.
     *
     * @param platform The platform name.
     * @param callback The callback parameters.
     * @return The authenticated user info.
     */
    ThirdAuthUser login(String platform, ThirdAuthCallback callback);
}
