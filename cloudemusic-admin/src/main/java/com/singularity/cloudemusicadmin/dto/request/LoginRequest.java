package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    private String username;
    private String password;
}
