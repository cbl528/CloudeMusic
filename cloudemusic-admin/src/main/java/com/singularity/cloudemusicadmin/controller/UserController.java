package com.singularity.cloudemusicadmin.controller;

import com.singularity.cloudemusicadmin.common.CurrentUserId;
import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(@CurrentUserId Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@CurrentUserId Long userId,
                                                   @RequestBody UserInfoUpdateRequest body) {
        userService.updateUserInfo(userId, body);
        return Result.success();
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount(@CurrentUserId Long userId) {
        userService.deleteAccount(userId);
        return Result.success();
    }
}
