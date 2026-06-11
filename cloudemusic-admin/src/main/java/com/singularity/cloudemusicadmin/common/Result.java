package com.singularity.cloudemusicadmin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应封装，所有接口通过该类返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private T data;
    private String message;

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, data, "操作成功");
    }

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return success(null);
    }

    /** 成功，自定义消息 */
    public static <T> Result<T> success(String message) {
        return new Result<T>(200, null, message);
    }

    /** 失败，自定义状态码和消息 */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    /** 失败，默认 500 状态码 */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, null, message);
    }
}
