package com.usoft.framework.security.cas.config;

import java.io.IOException;

import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import com.usoft.framework.security.cas.user.CasUserDetailsService;
import com.usoft.framework.security.jwt.JwtTokenProvider;
import com.usoft.framework.security.user.AuthorizedUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * CAS Security 配置
 */
@Configuration
@ConditionalOnProperty(prefix = "usoft.security.cas", name = "enabled", havingValue = "true")
@Slf4j
public class CasSecurityConfig {

    @Autowired
    private CasProperties casProperties;

    @Autowired
    private CasUserDetailsService casUserDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casProperties.getClientHostUrl() + casProperties.getClientLoginUrl());
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    TicketValidator ticketValidator() {
        return new Cas30ServiceTicketValidator(casProperties.getServerUrlPrefix());
    }

    @Bean
    CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setAuthenticationUserDetailsService(casUserDetailsService);
        provider.setServiceProperties(serviceProperties());
        provider.setTicketValidator(ticketValidator());
        provider.setKey("CAS_PROVIDER_KEY");
        return provider;
    }

    @Bean
    CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl(casProperties.getServerLoginUrl());
        entryPoint.setServiceProperties(serviceProperties());
        return entryPoint;
    }

    @Bean
    CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager) {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl(casProperties.getClientLoginUrl());

        // 认证成功后的处理：生成JWT并重定向到前端
        filter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                if (authentication.getPrincipal() instanceof AuthorizedUser user) {
                    String token = jwtTokenProvider.generateToken(user);
                    String refreshToken = jwtTokenProvider.generateRefreshToken(user);

                    String targetUrl = casProperties.getFrontendCallbackUrl();
                    if (targetUrl == null || targetUrl.isEmpty()) {
                        targetUrl = "/";
                    }

                    // 将token作为参数附加到URL
                    // 注意：实际生产中建议使用更安全的方式传递token，例如设置Cookie或通过中间页postMessage
                    String redirectUrl = targetUrl + "?token=" + token + "&refreshToken=" + refreshToken;
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect("/error?message=LoginFailed");
                }
            }
        });

        filter.setAuthenticationFailureHandler(
                new SimpleUrlAuthenticationFailureHandler("/error?message=CasLoginFailed"));
        return filter;
    }

    @Bean
    org.springframework.boot.web.servlet.FilterRegistrationBean<CasAuthenticationFilter> casAuthenticationFilterRegistration(
            CasAuthenticationFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain casSecurityFilterChain(HttpSecurity http, CasAuthenticationFilter casAuthenticationFilter,
            CasAuthenticationEntryPoint casAuthenticationEntryPoint) throws Exception {
        http
                .securityMatcher(casProperties.getClientLoginUrl())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webapp/**", "/favicon.ico").permitAll()
                        .requestMatchers(
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET,
                                        "/file/[\\w-/]+\\.(png|jpg|jpeg|gif|webp|svg)"),
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/auth/third/url/\\w+"),
                                RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/auth/third/render/\\w+"),
                                RegexRequestMatcher.regexMatcher("/api/auth/third/callback/\\w+"))
                        .permitAll()
                        .requestMatchers("/api/auth/cas/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bi/pages/share-schema/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/icons/*.js").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/client-login",
                                "/api/auth/client-refresh-token")
                        .permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilter(casAuthenticationFilter)
                .exceptionHandling(e -> e.authenticationEntryPoint(casAuthenticationEntryPoint));

        return http.build();
    }
}
