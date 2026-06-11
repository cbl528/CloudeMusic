package com.singularity.cloudemusicadmin.config;

import com.singularity.cloudemusicadmin.common.BusinessException;
import com.singularity.cloudemusicadmin.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，将异常统一转为 Result 格式返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常，按 BusinessException 中的 code 返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 处理其他未知异常，返回 500 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error("服务器内部错误：" + e.getMessage());
    }
}
