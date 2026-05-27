package com.usoft.framework.security.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.usoft.framework.core.security.UserDetailsWithId;
import com.usoft.framework.system.api.TenantResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 授权用户
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizedUser implements UserDetailsWithId {

    private String id;

    private String departmentId;

    private String username;

    private String displayName;

    private String avatar;

    private String password;

    private List<TenantResponse> tenants;

    private List<String> roles;

    private List<String> permissions;

    private String activedTenantId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.addAll(roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        authorities.addAll(permissions.stream().map(permission -> new SimpleGrantedAuthority(permission)).toList());
        return authorities;
    }

    public boolean containsTenant(String tenantId) {
        return tenants != null && tenants.stream().anyMatch(tenant -> tenant.getId().equals(tenantId));
    }
}
