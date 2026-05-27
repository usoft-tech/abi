package com.usoft.framework.security.cas.user;

import java.util.List;
import java.util.Map;

import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.usoft.framework.common.enums.EnableStatus;
import com.usoft.framework.security.user.AuthorizedUser;
import com.usoft.framework.system.api.PermissionResponse;
import com.usoft.framework.system.api.RoleResponse;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.system.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CAS 用户详情服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        String username = token.getName();
        log.debug("Loading user by CAS username: {}", username);
        
        // 获取CAS返回的属性
        Map<String, Object> attributes = token.getAssertion().getPrincipal().getAttributes();
        log.debug("CAS attributes: {}", attributes);

        // 查询本地用户
        UserEntity user = userMapper.findByUsername(username);
        if (user == null) {
            // 这里可以选择自动创建用户，或者抛出异常
            // 暂时抛出异常，如果需要自动创建可以添加逻辑
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<TenantResponse> tenants = userService.listTenantsByUserId(user.getId())
                .stream()
                .filter(tenant -> tenant.getStatus() == EnableStatus.ENABLE)
                .toList();
        List<RoleResponse> roles = userService.listRoleByUserId(user.getId());
        List<String> permissions = userService.listPermissionByUserId(user.getId())
                .stream()
                .map(PermissionResponse::getCode)
                .toList();

        return AuthorizedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .password(user.getPassword()) // 密码可能为空或无效，因为是CAS登录
                .departmentId(user.getDepartmentId())
                .tenants(tenants)
                .roles(roles.stream().map(RoleResponse::getCode).toList())
                .permissions(permissions)
                .build();
    }
}
