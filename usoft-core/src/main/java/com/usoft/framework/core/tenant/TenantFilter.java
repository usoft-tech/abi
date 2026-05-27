package com.usoft.framework.core.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 多租户过滤器，读取请求头 X-Tenant-ID 并设置上下文
 */
public class TenantFilter extends HttpFilter {
    private static final String HEADER_TENANT_ID = "X-Tenant-ID";

    /**
     * 过滤请求以提取租户ID
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String tenantId = request.getHeader(HEADER_TENANT_ID);
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

