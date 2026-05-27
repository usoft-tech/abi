package com.usoft.framework.common.exception;

import lombok.Getter;

@Getter
public class HttpStatusException extends RuntimeException {

    private final int status;

    public HttpStatusException(int status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
