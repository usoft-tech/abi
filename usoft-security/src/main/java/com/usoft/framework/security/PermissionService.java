package com.usoft.framework.security;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.usoft.framework.security.user.AuthorizedUser;

/**
 * 权限服务
 */
@Component("ss")
public class PermissionService {

    /**
     * 是否有权限
     *
     * @param authority 权限
     * @return 是否有权限
     */
    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthorizedUser authorizedUser) {
            // 超管直接返回true
            if ("1".equals(authorizedUser.getId())) {
                return true;
            }
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> StringUtils.equalsAny(grantedAuthority.getAuthority(), authority,
                        // 超管权限
                        "ROLE_ADMIN",
                        // 所有权限
                        "*:*:*"));
    }

    /**
     * 是否有任意权限
     *
     * @param authorities 权限列表
     * @return 是否有任意权限
     */
    public boolean hasAnyAuthority(String... authorities) {
        return Arrays.stream(authorities).anyMatch(this::hasAuthority);
    }
}
