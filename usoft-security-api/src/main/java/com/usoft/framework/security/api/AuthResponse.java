package com.usoft.framework.security.api;

import java.util.List;

import com.usoft.framework.system.api.TenantResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";

    private List<TenantResponse> tenants;

    public AuthResponse(String token, List<TenantResponse> tenants) {
        this.token = token;
        this.tenants = tenants;
    }

    public AuthResponse(String token, String refreshToken, List<TenantResponse> tenants) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tenants = tenants;
    }
}
