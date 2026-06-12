package com.singularity.cloudemusicadmin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.singularity.cloudemusicadmin.common.BusinessException;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.LoginResponse;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.entity.User;
import com.singularity.cloudemusicadmin.mapper.UserMapper;
import com.singularity.cloudemusicadmin.service.MinioService;
import com.singularity.cloudemusicadmin.service.UserService;
import com.singularity.cloudemusicadmin.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MinioService minioService;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查询用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 生成并返回 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginResponse build = LoginResponse.builder()
                .token(token)
                .build();

        return build;
    }

    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));

        if (count != null && count > 0) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 构建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);

        int insert = userMapper.insert(user);
        if(insert < 1){
            throw new BusinessException(500, "注册失败");
        }
        return user;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toUserInfoResponse(user);
    }

    @Override
    public void updateUserInfo(Long userId, UserInfoUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        int i = userMapper.updateById(user);
        if (i < 1) {
            throw new BusinessException(500, "更新用户信息失败");
        }
    }

    @Override
    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public UserInfoResponse uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 上传到 MinIO
        String avatarUrl = minioService.uploadAvatar(file, userId);

        // 更新用户 avatar 字段
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);

        return toUserInfoResponse(user);
    }

    /** 将 User 实体转换为安全的响应 DTO */
    private UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setGender(user.getGender());
        resp.setSignature(user.getSignature());
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }
}
