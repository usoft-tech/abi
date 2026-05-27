package com.usoft.framework.security.thirdauth.controller;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.security.api.AuthResponse;
import com.usoft.framework.security.thirdauth.config.ThirdAuthProperties;
import com.usoft.framework.security.thirdauth.model.ThirdAuthCallback;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;
import com.usoft.framework.security.thirdauth.service.ThirdAuthService;
import com.usoft.framework.security.thirdauth.service.ThirdAuthUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controller for third-party authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/third")
@RequiredArgsConstructor
public class ThirdAuthController {

    private final ThirdAuthService thirdAuthService;
    private final ThirdAuthUserService thirdAuthUserService;
    private final ThirdAuthProperties properties;

    /**
     * Get the authorization URL.
     *
     * @param platform The platform name.
     * @return The authorization URL.
     */
    @GetMapping("/url/{platform}")
    public ApiResponse<String> getAuthUrl(@PathVariable("platform") String platform) {
        return ApiResponse.ok(thirdAuthService.getAuthorizeUrl(platform));
    }

    /**
     * Redirect to the third-party authorization page.
     *
     * @param platform The platform name (e.g., github, wechat).
     * @param response The HTTP response.
     * @throws IOException If redirect fails.
     */
    @GetMapping("/render/{platform}")
    public void render(@PathVariable("platform") String platform, HttpServletResponse response) throws IOException {
        String url = thirdAuthService.getAuthorizeUrl(platform);
        response.sendRedirect(url);
    }

    /**
     * Handle the callback from the third-party platform.
     *
     * @param platform The platform name.
     * @param callback The callback parameters.
     * @param response The HTTP response.
     * @throws IOException If redirect fails.
     */
    @RequestMapping("/callback/{platform}")
    public void callback(@PathVariable("platform") String platform, ThirdAuthCallback callback, HttpServletResponse response) throws IOException {
        try {
            ThirdAuthUser user = thirdAuthService.login(platform, callback);
            AuthResponse authResponse = thirdAuthUserService.login(platform, user);

            String targetUrl = properties.getFrontendUrl() + "?token=" + authResponse.getToken() 
                    + "&refreshToken=" + authResponse.getRefreshToken();
            
            // Append tenants info? Maybe too long for URL. Frontend can fetch user info using token.
            
            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("Third-party login failed", e);
            response.sendRedirect(properties.getFrontendUrl() + "?error=" + e.getMessage());
        }
    }
}
