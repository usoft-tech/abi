package com.usoft.framework.security.thirdauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Token information from third-party authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdAuthToken implements Serializable {
    private String accessToken;
    private int expireIn;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String uid;
    private String openId;
    private String unionId;
}
