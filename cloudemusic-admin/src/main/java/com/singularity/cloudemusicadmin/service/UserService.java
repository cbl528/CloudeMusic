package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.dto.request.LoginRequest;
import com.singularity.cloudemusicadmin.dto.request.RegisterRequest;
import com.singularity.cloudemusicadmin.entity.User;

public interface UserService {

    String login(LoginRequest request);

    User register(RegisterRequest request);
}
