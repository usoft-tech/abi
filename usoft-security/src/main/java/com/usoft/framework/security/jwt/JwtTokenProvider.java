package com.usoft.framework.security.jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.usoft.framework.security.user.AuthorizedUser;
import com.usoft.framework.system.api.TenantResponse;
import com.usoft.framework.utils.ObjectMapperUtils;

/**
 * JWT令牌生成与验证组件
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_DISPLAY_NAME = "displayName";
    private static final String CLAIM_DEPARTMENT_ID = "departmentId";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TENANTS = "tenants";
    private static final String CLAIM_ACTIVED_TENANT_ID = "activedTenantId";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.ttlSeconds:3600}")
    private long ttlSeconds;

    @Value("${security.jwt.refresh-ttlSeconds:604800}")
    private long refreshTtlSeconds;

    /**
     * 生成JWT令牌
     * 
     * @param user 授权用户
     * @return JWT令牌字符串
     */
    public String generateToken(AuthorizedUser user) {
        try {
            Instant now = Instant.now();
            List<String> roles = user.getRoles() != null ? user.getRoles() : List.of();
            List<String> permissions = user.getPermissions() != null ? user.getPermissions() : List.of();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim(CLAIM_USER_ID, user.getId())
                    .claim(CLAIM_USERNAME, user.getUsername())
                    .claim(CLAIM_DISPLAY_NAME, user.getDisplayName())
                    .claim(CLAIM_DEPARTMENT_ID, user.getDepartmentId())
                    .claim(CLAIM_ROLES, roles)
                    .claim(CLAIM_PERMISSIONS, permissions)
                    .claim(CLAIM_TENANTS, ObjectMapperUtils.toJson(Objects.requireNonNullElse(user.getTenants(), Collections.emptyList())))
                    .claim(CLAIM_ACTIVED_TENANT_ID, user.getActivedTenantId())
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret.getBytes()));
            return jwt.serialize();
        } catch (Exception e) {
            logger.error("JWT generate failed", e);
            throw new IllegalStateException("JWT generate failed: " + e.getMessage());
        }
    }

    /**
     * 生成刷新令牌
     *
     * @param user 授权用户
     * @return 刷新令牌字符串
     */
    public String generateRefreshToken(AuthorizedUser user) {
        try {
            Instant now = Instant.now();
            List<String> roles = user.getRoles() != null ? user.getRoles() : List.of();
            List<String> permissions = user.getPermissions() != null ? user.getPermissions() : List.of();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(refreshTtlSeconds)))
                    .claim(CLAIM_USER_ID, user.getId())
                    .claim(CLAIM_USERNAME, user.getUsername())
                    .claim(CLAIM_DISPLAY_NAME, user.getDisplayName())
                    .claim(CLAIM_DEPARTMENT_ID, user.getDepartmentId())
                    .claim(CLAIM_ROLES, roles)
                    .claim(CLAIM_PERMISSIONS, permissions)
                    .claim(CLAIM_TENANTS, ObjectMapperUtils.toJson(Objects.requireNonNullElse(user.getTenants(), Collections.emptyList())))
                    .claim(CLAIM_ACTIVED_TENANT_ID, user.getActivedTenantId())
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret.getBytes()));
            return jwt.serialize();
        } catch (Exception e) {
            logger.error("JWT generate failed", e);
            throw new IllegalStateException("JWT generate failed: " + e.getMessage());
        }
    }

    /**
     * 验证并解析JWT令牌
     * 
     * @param token 令牌字符串
     * @return 解析得到的 AuthorizedUser，验证失败返回 null
     */
    public AuthorizedUser parseToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean verified = jwt.verify(new MACVerifier(secret.getBytes()));
            if (!verified) {
                logger.warn("JWT signature verification failed");
                return null;
            }
            if (!jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.HS256)) {
                logger.warn("JWT algorithm mismatch: expected HS256 but got {}", jwt.getHeader().getAlgorithm());
                return null;
            }
            // Check expiration
            if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                logger.warn("JWT expired");
                return null;
            }
            
            var claims = jwt.getJWTClaimsSet();
            String username = claims.getSubject();
            String displayName = claims.getStringClaim(CLAIM_DISPLAY_NAME);
            String userId = claims.getStringClaim(CLAIM_USER_ID);
            String departmentId = claims.getStringClaim(CLAIM_DEPARTMENT_ID);
            List<String> roles = claims.getStringListClaim(CLAIM_ROLES);
            List<String> permissions = claims.getStringListClaim(CLAIM_PERMISSIONS);
            String activedTenantId = claims.getStringClaim(CLAIM_ACTIVED_TENANT_ID);
            String tenantsJson = claims.getStringClaim(CLAIM_TENANTS);
            List<TenantResponse> tenants = ObjectMapperUtils.fromJson(tenantsJson, new TypeReference<List<TenantResponse>>() {});
            return AuthorizedUser.builder()
                    .id(userId)
                    .username(username)
                    .displayName(displayName)
                    .password("")
                    .departmentId(departmentId)
                    .activedTenantId(activedTenantId)
                    .tenants(tenants)
                    .roles(roles != null ? roles : List.of())
                    .permissions(permissions != null ? permissions : List.of())
                    .build();
        } catch (Exception e) {
            logger.error("JWT parse failed", e);
            return null;
        }
    }
}
