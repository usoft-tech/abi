package com.usoft.framework.security.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.security.api.AuthRequest;
import com.usoft.framework.security.api.AuthResponse;
import com.usoft.framework.security.api.SysAuthClientLoginRequest;
import com.usoft.framework.security.api.SysAuthClientRefreshRequest;
import com.usoft.framework.security.jwt.JwtTokenProvider;
import com.usoft.framework.security.user.AuthorizedUser;
import com.usoft.framework.security.utils.Sm2Utils;
import com.usoft.framework.system.api.SysAuthClientResponse;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.system.api.UserResponse;
import com.usoft.framework.system.service.SysAuthClientService;
import com.usoft.framework.system.service.UserService;

/**
 * 认证控制器
 */
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysAuthClientService sysAuthClientService;

    @Value("${security.sm2.private-key}")
    private String sm2PrivateKey;

    private final UserService userService;

    /**
     * 构造认证控制器
     */
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService, SysAuthClientService sysAuthClientService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.sysAuthClientService = sysAuthClientService;
    }

    /**
     * 用户登录并返回JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Validated @RequestBody AuthRequest req) {
        // 解密密码
        String password = req.getPassword();
        if (sm2PrivateKey != null && !sm2PrivateKey.isEmpty()) {
            try {
                password = Sm2Utils.decrypt(sm2PrivateKey, password);
            } catch (Exception e) {
                // 解密失败，可能是未加密的密码（兼容处理）或者攻击
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "用户名密码错误"));
            }
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), password));
        AuthorizedUser user = (AuthorizedUser) auth.getPrincipal();
        String token = tokenProvider.generateToken(user);
        return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(token, user.getTenants())));
    }

    /**
     * 客户端登录
     */
    @PostMapping("/client-login")
    public ResponseEntity<ApiResponse<AuthResponse>> clientLogin(@Validated @RequestBody SysAuthClientLoginRequest req) {
        SysAuthClientResponse client = sysAuthClientService.validateClient(req.getClientId(), req.getClientSecret());
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "客户端认证失败"));
        }
        
        TenantResponse tenant = new TenantResponse();
        tenant.setId(client.getTenantId());
        tenant.setName("Default");
        
        AuthorizedUser user = AuthorizedUser.builder()
                .id(client.getClientId())
                .username(client.getClientId())
                .displayName(client.getName())
                .password("")
                .activedTenantId(client.getTenantId())
                .tenants(List.of(tenant))
                .roles(List.of("CLIENT"))
                .permissions(List.of())
                .build();
                
        String token = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        
        return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(token, refreshToken, user.getTenants())));
    }
    
    /**
     * 客户端刷新令牌
     */
    @PostMapping("/client-refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> clientRefreshToken(@Validated @RequestBody SysAuthClientRefreshRequest req) {
        AuthorizedUser user = tokenProvider.parseToken(req.getRefreshToken());
        if (user == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "无效的刷新令牌"));
        }
        
        String token = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        
        return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(token, refreshToken, user.getTenants())));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user-info")
    public ApiResponse<AuthorizedUser> userInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        AuthorizedUser user = (AuthorizedUser) authentication.getPrincipal();
        UserResponse userResponse = userService.get(user.getId());
        user.setAvatar(userResponse.getAvatar());
        user.setDisplayName(userResponse.getDisplayName());
        return ApiResponse.ok(user);
    }
}
