package com.usoft.framework.system.aspect;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.usoft.framework.core.tenant.TenantContext;
import com.usoft.framework.system.entity.OperationLogEntity;
import com.usoft.framework.system.service.OperationLogService;
import com.usoft.framework.utils.ObjectMapperUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 操作日志切面，监控控制器调用并记录
 */
@Aspect
@Component
public class OperationLogAspect {
    private final OperationLogService logService;

    /**
     * 构造切面
     */
    public OperationLogAspect(OperationLogService logService) {
        this.logService = logService;
    }

    /**
     * 环绕通知，记录接口调用日志
     */
    @Around("execution(* com.usoft.framework..controller..*(..))")
    public Object aroundControllers(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String methodName = sig.getMethod().getName();
        String className = sig.getDeclaringType().getSimpleName();
        String username = resolveUsername();
        String tenantId = TenantContext.getTenantId();
        OperationLogEntity log = new OperationLogEntity();
        log.setModule(className);
        log.setAction(methodName);
        log.setMethod(sig.getMethod().toGenericString());
        log.setUsername(username);
        log.setTenantId(tenantId);
        log.setCreatedAt(Instant.now());
        log.setCreatedBy(username);
        log.setRequestBody(ObjectMapperUtils.toJson(sanitizeArgs(pjp.getArgs())));
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            log.setDurationMs(duration);
            log.setStatus("SUCCESS");
            log.setResponseBody(ObjectMapperUtils.toJson(sanitizeResult(result)));
            logService.save(log);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.setDurationMs(duration);
            log.setStatus("ERROR");
            log.setResponseBody(ex.getMessage());
            logService.save(log);
            throw ex;
        }
    }

    /**
     * 解析当前用户名
     */
    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "anonymous";
    }

    /**
     * 处理请求入参，过滤不可序列化内容
     */
    private Object sanitizeArgs(Object[] args) {
        if (args == null) {
            return List.of();
        }
        return Arrays.stream(args).map(this::sanitizeObject).filter(Objects::nonNull).toList();
    }

    /**
     * 处理响应结果，过滤不可序列化内容
     */
    private Object sanitizeResult(Object result) {
        return sanitizeObject(result);
    }

    /**
     * 对对象进行安全序列化转换
     */
    private Object sanitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof MultipartFile mf) {
            return Map.of(
                    "filename", mf.getOriginalFilename(),
                    "size", mf.getSize(),
                    "contentType", mf.getContentType());
        }
        if (obj instanceof MultipartFile[] arr) {
            return Arrays.stream(arr).map(this::sanitizeObject).toList();
        }
        String cn = obj.getClass().getName();
        if (StringUtils.equalsAny(cn,
                "org.springframework.security.web.servletapi.HttpServlet3RequestFactory$Servlet3SecurityContextHolderAwareRequestWrapper",
                "org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse")) {
            return null;
        }
        if (cn.startsWith("jakarta.servlet") || cn.startsWith("javax.servlet")) {
            return cn;
        }
        if (cn.startsWith("org.springframework.web.multipart.support.StandardMultipartHttpServletRequest")) {
            return cn;
        }
        return obj;
    }
}
