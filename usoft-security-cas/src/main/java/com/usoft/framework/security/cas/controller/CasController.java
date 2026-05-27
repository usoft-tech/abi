package com.usoft.framework.security.cas.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.security.cas.config.CasProperties;

import jakarta.servlet.http.HttpServletResponse;

/**
 * CAS 认证控制器
 */
@RestController
@RequestMapping("/api/auth/cas")
public class CasController {

    @Autowired
    private CasProperties casProperties;

    /**
     * 获取 CAS 登录 URL
     */
    @GetMapping("/url")
    public ApiResponse<String> getLoginUrl() {
        String service = casProperties.getClientHostUrl() + casProperties.getClientLoginUrl();
        String loginUrl = casProperties.getServerLoginUrl() + "?service=" + service;
        return ApiResponse.ok(loginUrl);
    }
    
    /**
     * 重定向到 CAS 登录
     */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String service = casProperties.getClientHostUrl() + casProperties.getClientLoginUrl();
        String loginUrl = casProperties.getServerLoginUrl() + "?service=" + service;
        response.sendRedirect(loginUrl);
    }
}
