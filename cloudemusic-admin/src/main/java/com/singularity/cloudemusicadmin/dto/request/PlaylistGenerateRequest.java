package com.singularity.cloudemusicadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaylistGenerateRequest {
    @NotBlank(message = "请输入心情或场景关键词")
    private String keyword;
}
