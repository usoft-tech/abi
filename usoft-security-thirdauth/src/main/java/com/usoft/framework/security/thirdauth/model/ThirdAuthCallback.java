package com.usoft.framework.security.thirdauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Unified callback parameters for third-party authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdAuthCallback implements Serializable {
    /**
     * Authorization code.
     */
    private String code;

    /**
     * Authorization code (some platforms use auth_code).
     */
    private String auth_code;

    /**
     * State parameter for CSRF protection.
     */
    private String state;
    
    /**
     * Access token (some platforms return token directly).
     */
    private String authorization_code;
    
    /**
     * Twitter oauth_token.
     */
    private String oauth_token;
    
    /**
     * Twitter oauth_verifier.
     */
    private String oauth_verifier;
}
