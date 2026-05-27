package com.usoft.framework.security.thirdauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Unified user information from third-party authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdAuthUser implements Serializable {
    /**
     * Unique ID from the platform (e.g., OpenID or UnionID).
     */
    private String uuid;

    /**
     * Username (if available).
     */
    private String username;

    /**
     * Nickname.
     */
    private String nickname;

    /**
     * Avatar URL.
     */
    private String avatar;

    /**
     * Email (if available).
     */
    private String email;

    /**
     * Gender (MALE, FEMALE, UNKNOWN).
     */
    private String gender;

    /**
     * Location.
     */
    private String location;

    /**
     * Platform source (e.g., "wechat", "github").
     */
    private String source;

    /**
     * Raw user info returned by the platform.
     */
    private Map<String, Object> rawUserInfo;

    /**
     * Access token information.
     */
    private ThirdAuthToken token;

}
