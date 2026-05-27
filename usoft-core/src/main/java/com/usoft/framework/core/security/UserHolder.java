package com.usoft.framework.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户持有者，用于获取当前请求的用户名
 */
public final class UserHolder {

    private UserHolder() {}

    /**
     * 获取当前用户名
     */
    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    /**
     * 获取当前认证对象
     */
    public static UserDetailsWithId principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetailsWithId userDetailsWithId) {
            return userDetailsWithId;
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static String userId() {
        UserDetailsWithId principal = principal();
        return principal != null ? principal.getId() : null;
    }
}

