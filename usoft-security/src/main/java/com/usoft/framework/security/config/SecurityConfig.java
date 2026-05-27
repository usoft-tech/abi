package com.usoft.framework.security.config;

import java.io.IOException;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import com.usoft.framework.core.tenant.TenantFilter;
import com.usoft.framework.security.jwt.JwtAuthenticationFilter;
import com.usoft.framework.utils.ObjectMapperUtils;
import com.usoft.framework.common.api.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security安全配置
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 配置 Security 过滤链
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtAuthenticationFilter jwtFilter,
            TenantFilter tenantFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                // 禁用基本的HTTP认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用注销功能
                .logout(AbstractHttpConfigurer::disable)
                // 禁用frame选项，防止点击劫持攻击
                .headers(spec -> spec.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // .sessionManagement(sm ->
                // sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 设置认证管理器
                .authenticationManager(authenticationManager)
                // 配置异常处理：访问拒绝处理器和认证入口点
                .exceptionHandling(customizer -> customizer.accessDeniedHandler(this::accessDeniedHandler)
                        .authenticationEntryPoint(this::authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/webapp/**", "/favicon.ico").permitAll()
                        .requestMatchers(
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET,
                                        "/file/[\\w-/]+\\.(png|jpg|jpeg|gif|webp|svg)"),
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/auth/third/url/\\w+"),
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/auth/third/render/\\w+"),
                                RegexRequestMatcher.regexMatcher("/api/auth/third/callback/\\w+"))
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bi/pages/share-schema/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/icons/*.js").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/client-login",
                                "/api/auth/client-refresh-token")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 暴露认证管理器
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 租户过滤器 Bean
     */
    @Bean
    TenantFilter tenantFilter() {
        return new TenantFilter();
    }

    /**
     * 密码编码器
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 处理访问拒绝异常的函数
     *
     * @param request               请求
     * @param response              响应
     * @param accessDeniedException 访问拒绝异常
     */
    void accessDeniedHandler(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) {
        // 使用try-with-resources确保在使用完writer后自动关闭资源
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        try (var writer = response.getWriter()) {
            // 使用OBJECT_MAPPER将错误信息写入HTTP响应中
            ObjectMapperUtils.writeValue(writer, ApiResponse.fail(String.valueOf(HttpStatus.FORBIDDEN.value()),
                    Objects.requireNonNullElse(accessDeniedException.getLocalizedMessage(), "无权访问")));
        } catch (IOException ignored) {
            // 如果在写入过程中发生IOException，此处选择忽略异常
        }
    }

    /**
     * 处理未授权的请求
     *
     * @param request       请求
     * @param response      响应
     * @param authException 授权异常
     */
    void authenticationEntryPoint(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {
        // 使用try-with-resources确保在使用完writer后自动关闭资源
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        try (var writer = response.getWriter()) {
            // 使用OBJECT_MAPPER将错误信息写入HTTP响应中
            ObjectMapperUtils.writeValue(writer, ApiResponse.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                    Objects.requireNonNullElse(authException.getLocalizedMessage(), "未授权")));
        } catch (IOException ignored) {
            // 如果在写入过程中发生IOException，此处选择忽略异常
        }
    }
}
