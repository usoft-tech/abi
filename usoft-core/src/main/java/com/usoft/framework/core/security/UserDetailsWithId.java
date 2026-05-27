package com.usoft.framework.core.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsWithId extends UserDetails {

    /**
     * 获取用户ID
     */
    String getId();
}
