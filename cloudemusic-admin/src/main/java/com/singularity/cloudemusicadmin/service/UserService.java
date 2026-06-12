package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.dto.request.ChangePasswordRequest;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.LoginResponse;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 登录，返回 JWT token */
    LoginResponse login(LoginRequest request);

    /** 注册，返回用户信息 */
    User register(RegisterRequest request);

    /** 获取当前用户信息 */
    UserInfoResponse getUserInfo(Long userId);

    /** 更新用户信息，返回更新后的用户数据 */
    UserInfoResponse updateUserInfo(Long userId, UserInfoUpdateRequest request);

    /** 注销账号（逻辑删除） */
    void deleteAccount(Long userId);

    /** 上传头像到 MinIO 并更新用户 avatar 字段 */
    UserInfoResponse uploadAvatar(Long userId, MultipartFile file);

    /** 修改密码 */
    void changePassword(Long userId, ChangePasswordRequest request);
}
