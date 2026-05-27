package com.usoft.framework.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.usoft.framework.common.api.ApiResponse;
import com.usoft.framework.common.exception.BizException;
import com.usoft.framework.common.exception.HttpStatusException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleBizException(BizException e) {
        logger.warn("Biz error: {}", e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation error: {}", e.getMessage());
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleBindException(BindException e) {
        logger.warn("Bind error: {}", e.getMessage());
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), message);
    }

    /**
     * 处理认证异常 - 重新抛出给 Security 处理
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleAuthenticationException(AuthenticationException e) throws AuthenticationException {
        logger.warn("Authentication error: {}", e.getMessage());
        throw e;
    }

    /**
     * 处理权限异常 - 重新抛出给 Security 处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleAccessDeniedException(AccessDeniedException e) throws AccessDeniedException {
        logger.warn("Access denied error: {}", e.getMessage());
        throw e;
    }

    /**
     * 处理 Http 状态异常
     */
    @ExceptionHandler(HttpStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpStatusException(HttpStatusException e) {
        logger.error("HttpStatusException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.fail(String.valueOf(e.getStatus()), e.getMessage()));
    }

    /**
     * 处理静态资源未找到异常 (Spring Boot 3)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNoResourceFoundException(NoResourceFoundException e) {
        logger.warn("Resource not found: {}", e.getResourcePath());
        return ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "资源不存在: " + e.getResourcePath());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        logger.error("Unhandled exception", e);
        return ApiResponse.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "服务器内部错误");
    }
}
