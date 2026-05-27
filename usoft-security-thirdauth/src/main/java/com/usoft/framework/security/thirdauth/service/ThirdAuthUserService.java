package com.usoft.framework.security.thirdauth.service;

import com.usoft.framework.security.api.AuthResponse;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;

/**
 * Service for handling third-party user login and binding.
 */
public interface ThirdAuthUserService {
    /**
     * Login or register a third-party user.
     *
     * @param platform The platform name.
     * @param user     The third-party user info.
     * @return The authentication response (token).
     */
    AuthResponse login(String platform, ThirdAuthUser user);
}
