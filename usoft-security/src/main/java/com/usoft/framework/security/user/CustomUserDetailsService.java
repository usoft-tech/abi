package com.usoft.framework.security.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.usoft.framework.system.api.PermissionResponse;
import com.usoft.framework.system.api.RoleResponse;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.system.service.UserService;
import com.usoft.framework.common.enums.EnableStatus;

/**
 * 用户详情服务，基于数据库加载用户
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    private final UserService userService;


    /**
     * 构造服务
     */
    public CustomUserDetailsService(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * 根据用户名加载用户
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        List<TenantResponse> tenants = userService.listTenantsByUserId(user.getId())
                .stream()
                .filter(tenant -> tenant.getStatus() == EnableStatus.ENABLE)
                .toList();
        List<String> roles = new ArrayList<>(userService.listRoleByUserId(user.getId()).stream().map(RoleResponse::getCode).toList());
        if (StringUtils.equals(user.getId(), "1")) {
            roles.add("admin");
        }
        List<String> permissions = userService.listPermissionByUserId(user.getId())
                .stream()
                .map(PermissionResponse::getCode)
                .toList();

        return AuthorizedUser
                 .builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .password(user.getPassword())
                .departmentId(user.getDepartmentId())
                .tenants(tenants)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
