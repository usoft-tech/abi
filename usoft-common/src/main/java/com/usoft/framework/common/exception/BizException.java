package com.usoft.framework.common.exception;

import lombok.Getter;

/**
 * 业务异常定义
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;

    public BizException(String message) {
        super(message);
        this.code = "error";
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = "error";
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}


