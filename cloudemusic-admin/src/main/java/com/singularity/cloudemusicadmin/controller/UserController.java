package com.singularity.cloudemusicadmin.controller;

import com.singularity.cloudemusicadmin.common.CurrentUserId;
import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.ChangePasswordRequest;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.dto.request.SongActionRequest;
import com.singularity.cloudemusicadmin.dto.request.UserInfoUpdateRequest;
import com.singularity.cloudemusicadmin.dto.response.LoginResponse;
import com.singularity.cloudemusicadmin.dto.response.UserInfoResponse;
import com.singularity.cloudemusicadmin.entity.Favorite;
import com.singularity.cloudemusicadmin.entity.History;
import com.singularity.cloudemusicadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/password")
    public Result<Void> changePassword(@CurrentUserId Long userId,
                                       @RequestBody ChangePasswordRequest body) {
        userService.changePassword(userId, body);
        return Result.success();
    }

    // ========== 收藏 ==========

    @PostMapping("/favorite")
    public Result<Void> addFavorite(@CurrentUserId Long userId,
                                    @RequestBody SongActionRequest body) {
        userService.addFavorite(userId, body);
        return Result.success();
    }

    @DeleteMapping("/favorite/{songId}")
    public Result<Void> removeFavorite(@CurrentUserId Long userId,
                                       @PathVariable Long songId) {
        userService.removeFavorite(userId, songId);
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<List<Favorite>> getFavorites(@CurrentUserId Long userId) {
        return Result.success(userService.getFavorites(userId));
    }

    @GetMapping("/favorite/{songId}")
    public Result<Map<String, Boolean>> checkFavorite(@CurrentUserId Long userId,
                                                       @PathVariable Long songId) {
        boolean favorited = userService.checkFavorite(userId, songId);
        return Result.success(Map.of("favorited", favorited));
    }

    // ========== 播放历史 ==========

    @PostMapping("/history")
    public Result<Void> addHistory(@CurrentUserId Long userId,
                                   @RequestBody SongActionRequest body) {
        userService.addHistory(userId, body);
        return Result.success();
    }

    @GetMapping("/history")
    public Result<List<History>> getHistory(@CurrentUserId Long userId) {
        return Result.success(userService.getHistory(userId));
    }

    @DeleteMapping("/history")
    public Result<Void> clearHistory(@CurrentUserId Long userId) {
        userService.clearHistory(userId);
        return Result.success();
    }
}
