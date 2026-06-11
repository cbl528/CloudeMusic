package com.singularity.cloudemusicadmin.common;

import lombok.Getter;

/**
 * 业务异常，全局异常处理器会将其转为统一响应返回
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
