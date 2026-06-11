package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterRequest {

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer gender;
    private String signature;
}
