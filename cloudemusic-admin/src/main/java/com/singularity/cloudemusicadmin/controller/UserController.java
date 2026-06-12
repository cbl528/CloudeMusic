package com.singularity.cloudemusicadmin.controller;

import com.singularity.cloudemusicadmin.common.CurrentUserId;
import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.LoginResponse;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cloude/music/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
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

    @PutMapping("/update")
    public Result<UserInfoResponse> updateUserInfo(@CurrentUserId Long userId,
                                                   @RequestBody UserInfoUpdateRequest body) {
        return Result.success(userService.updateUserInfo(userId, body));
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount(@CurrentUserId Long userId) {
        userService.deleteAccount(userId);
        return Result.success();
    }

    @PostMapping("/avatar/upload")
    public Result<UserInfoResponse> uploadAvatar(@CurrentUserId Long userId,
                                                  @RequestParam("file") MultipartFile file) {
        return Result.success(userService.uploadAvatar(userId, file));
    }
}
