package com.mz.blog.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public BusinessException(int code, String message) {
        this(code, HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(int code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(40400, HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(40300, HttpStatus.FORBIDDEN, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(40900, HttpStatus.CONFLICT, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(40001, HttpStatus.BAD_REQUEST, message);
    }
}
