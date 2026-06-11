package com.singularity.cloudemusicadmin.controller;

import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cloude/music/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        return Result.success(token);
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }
}
