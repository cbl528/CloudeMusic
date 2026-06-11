package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.entity.User;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 登录，返回 JWT token */
    String login(LoginRequest request);

    /** 注册，返回用户信息 */
    User register(RegisterRequest request);
}
