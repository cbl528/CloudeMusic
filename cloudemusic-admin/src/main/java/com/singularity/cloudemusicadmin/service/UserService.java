package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.entity.User;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 登录，返回 JWT token */
    String login(LoginRequest request);

    /** 注册，返回用户信息 */
    User register(RegisterRequest request);

    /** 获取当前用户信息 */
    UserInfoResponse getUserInfo(Long userId);

    /** 更新用户信息 */
    void updateUserInfo(Long userId, UserInfoUpdateRequest request);

    /** 注销账号（逻辑删除） */
    void deleteAccount(Long userId);
}
