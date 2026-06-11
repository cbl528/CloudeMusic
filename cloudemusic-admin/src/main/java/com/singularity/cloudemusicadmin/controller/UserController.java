package com.singularity.cloudemusicadmin.controller;

import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户登录注册控制器
 */
@RestController
@RequestMapping("/api/cloude/music/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return Result.success(token);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }
}
