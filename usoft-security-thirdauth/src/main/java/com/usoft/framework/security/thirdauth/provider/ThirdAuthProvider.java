package com.usoft.framework.security.thirdauth.provider;

import com.usoft.framework.security.thirdauth.model.ThirdAuthCallback;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;

/**
 * Interface for third-party authentication providers.
 */
public interface ThirdAuthProvider {

    /**
     * Get the platform name (e.g., github, wechat).
     *
     * @return The platform name.
     */
    String getPlatform();

    /**
     * Get the authorization URL.
     *
     * @param state The state to prevent CSRF.
     * @return The authorization URL.
     */
    String getAuthorizeUrl(String state);

    /**
     * Login with the callback from the third-party platform.
     *
     * @param callback The callback parameters.
     * @return The authenticated user info.
     */
    ThirdAuthUser login(ThirdAuthCallback callback);
}
