package com.singularity.cloudemusicadmin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.singularity.cloudemusicadmin.common.BusinessException;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.entity.User;
import com.singularity.cloudemusicadmin.mapper.UserMapper;
import com.singularity.cloudemusicadmin.service.UserService;
import com.singularity.cloudemusicadmin.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String login(LoginRequest request) {
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
        return jwtUtil.generateToken(user.getId(), user.getUsername());
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
}
