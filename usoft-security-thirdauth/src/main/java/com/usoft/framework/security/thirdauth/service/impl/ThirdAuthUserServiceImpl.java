package com.usoft.framework.security.thirdauth.service.impl;

import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.usoft.framework.common.enums.EnableStatus;
import com.usoft.framework.common.utils.IdUtils;
import com.usoft.framework.security.api.AuthResponse;
import com.usoft.framework.security.jwt.JwtTokenProvider;
import com.usoft.framework.security.thirdauth.entity.ThirdAuthUserEntity;
import com.usoft.framework.security.thirdauth.mapper.ThirdAuthUserMapper;
import com.usoft.framework.security.thirdauth.model.ThirdAuthUser;
import com.usoft.framework.security.thirdauth.service.ThirdAuthUserService;
import com.usoft.framework.security.user.AuthorizedUser;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.api.enums.TenantAuthKey;
import com.usoft.framework.system.entity.TenantEntity;
import com.usoft.framework.system.entity.UserEntity;
import com.usoft.framework.system.entity.UserTenantEntity;
import com.usoft.framework.system.mapper.TenantMapper;
import com.usoft.framework.system.mapper.UserMapper;
import com.usoft.framework.system.mapper.UserTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdAuthUserServiceImpl implements ThirdAuthUserService {

    private final ThirdAuthUserMapper thirdAuthUserMapper;
    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;
    private final UserTenantMapper userTenantMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(String platform, ThirdAuthUser thirdUser) {
        // 1. Check if binding exists
        ThirdAuthUserEntity entity = thirdAuthUserMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq("source", platform)
                        .eq("uuid", thirdUser.getUuid())
        );

        UserEntity userEntity;
        if (entity != null) {
            // Binding exists, find user
            userEntity = userMapper.selectOneById(entity.getUserId());
            if (userEntity == null) {
                // User deleted but binding exists, recreate user
                userEntity = createUser(thirdUser);
                // Update binding
                entity.setUserId(userEntity.getId());
                entity.setUpdateTime(Instant.now());
                thirdAuthUserMapper.update(entity);
            } else {
                // Update binding time
                entity.setUpdateTime(Instant.now());
                thirdAuthUserMapper.update(entity);
            }
        } else {
            // Binding does not exist, create user and binding
            userEntity = createUser(thirdUser);
            // Create binding
            entity = ThirdAuthUserEntity.builder()
                    .userId(userEntity.getId())
                    .source(platform)
                    .uuid(thirdUser.getUuid())
                    .unionId(thirdUser.getToken() != null ? thirdUser.getToken().getUnionId() : null)
                    .username(thirdUser.getUsername())
                    .nickname(thirdUser.getNickname())
                    .avatar(thirdUser.getAvatar())
                    .rawData(JSON.toJSONString(thirdUser.getRawUserInfo()))
                    .createTime(Instant.now())
                    .updateTime(Instant.now())
                    .build();
            thirdAuthUserMapper.insert(entity);
        }

        // 2. Generate token
        return generateToken(userEntity);
    }

    private UserEntity createUser(ThirdAuthUser thirdUser) {
        UserEntity user = new UserEntity();
        user.setId(IdUtils.randomId());
        // Username must be unique. Use platform prefix or random.
        String username = thirdUser.getUsername();
        if (username == null || username.isEmpty()) {
            username = thirdUser.getSource() + "_" + thirdUser.getUuid().substring(0, Math.min(8, thirdUser.getUuid().length()));
        }
        // Check if username exists
        UserEntity existing = userMapper.findByUsername(username);
        if (existing != null) {
            username = username + "_" + IdUtils.randomId().substring(0, 4);
        }
        user.setUsername(username);
        user.setDisplayName(thirdUser.getNickname() != null ? thirdUser.getNickname() : username);
        user.setAvatar(thirdUser.getAvatar());
        user.setPassword(""); // No password
        user.setStatus(EnableStatus.ENABLE);
        user.setIsDeleted(false);
        user.setCreatedAt(Instant.now());
        user.setCreatedBy("system");
        
        userMapper.insert(user);

        // Bind to a default tenant?
        // Check if any tenant exists, if not create one
        List<TenantEntity> tenants = tenantMapper.selectAll();
        TenantEntity tenant;
        if (tenants.isEmpty()) {
            tenant = new TenantEntity();
            tenant.setId(IdUtils.randomId());
            tenant.setName("Default Tenant");
            tenant.setCreatedAt(Instant.now());
            tenant.setCreatedBy("system");
            tenantMapper.insert(tenant);
        } else {
            tenant = tenants.get(0);
        }

        UserTenantEntity ut = new UserTenantEntity();
        ut.setUserId(user.getId());
        ut.setTenantId(tenant.getId());
        ut.setAuthKey(TenantAuthKey.USER);
        userTenantMapper.insert(ut);

        return user;
    }

    private AuthResponse generateToken(UserEntity user) {
        // Find tenants for user via user_tenant table
        List<UserTenantEntity> userTenants = userTenantMapper.selectListByQuery(
                QueryWrapper.create().eq("user_id", user.getId())
        );
        
        List<TenantResponse> tenants = new ArrayList<>();
        if (!userTenants.isEmpty()) {
            List<String> tenantIds = userTenants.stream().map(UserTenantEntity::getTenantId).collect(Collectors.toList());
            List<TenantEntity> tenantEntities = tenantMapper.selectListByQuery(
                    QueryWrapper.create().in("id", tenantIds)
            );
            for (TenantEntity t : tenantEntities) {
                TenantResponse tr = new TenantResponse();
                tr.setId(t.getId());
                tr.setName(t.getName());
                tenants.add(tr);
            }
        }

        AuthorizedUser authorizedUser = AuthorizedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .password(user.getPassword())
                .activedTenantId(tenants.isEmpty() ? null : tenants.get(0).getId())
                .tenants(tenants)
                .roles(new ArrayList<>()) // Load roles if needed
                .permissions(new ArrayList<>()) // Load permissions if needed
                .build();

        String token = jwtTokenProvider.generateToken(authorizedUser);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authorizedUser);
        
        return new AuthResponse(token, refreshToken, tenants);
    }
}
