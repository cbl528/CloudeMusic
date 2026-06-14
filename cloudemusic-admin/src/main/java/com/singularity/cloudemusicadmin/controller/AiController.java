package com.singularity.cloudemusicadmin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.dto.request.PlaylistGenerateRequest;
import com.singularity.cloudemusicadmin.service.AiService;
import com.singularity.cloudemusicadmin.service.UserService;
import com.singularity.cloudemusicadmin.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/cloude/music/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/playlist/generate")
    public Result<JsonNode> generatePlaylist(
            @RequestBody @Valid PlaylistGenerateRequest request,
            HttpServletRequest httpRequest) {

        // 尝试从 token 中提取用户 ID（AI 功能不强制登录）
        Long userId = tryExtractUserId(httpRequest);

        // 如果已登录，获取其收藏作为个性化参考
        List<?> favorites = Collections.emptyList();
        if (userId != null) {
            favorites = userService.getFavorites(userId);
        }

        JsonNode result = aiService.generatePlaylist(request.getKeyword(), favorites);
        return Result.success(result);
    }

    /** 尝试从请求头中解析用户 ID，未登录时返回 null。 */
    private Long tryExtractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserId(token);
    }
}
