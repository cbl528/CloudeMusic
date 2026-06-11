package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;

/**
 * 用户信息更新请求 DTO
 */
@Data
public class UserInfoUpdateRequest {

    private String nickname;
    private String avatar;
    private String signature;
}
