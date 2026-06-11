package com.singularity.cloudemusicadmin.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.cloudemusicadmin.utils.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * JWT 认证过滤器，拦截 /api/cloude/music/user/* 路径的请求进行 token 校验
 */
@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_PATH = "/api/cloude/music/user/login";
    private static final String REGISTER_PATH = "/api/cloude/music/user/register";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // OPTIONS 预检请求直接放行（CORS 由 CorsFilter 处理）
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 登录和注册接口放行，无需 token
        if (path.equals(LOGIN_PATH) || path.equals(REGISTER_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        // 非 user 路径直接放行
        if (!path.startsWith("/api/cloude/music/user/")) {
            chain.doFilter(request, response);
            return;
        }

        // 提取并校验 token
        String authHeader = req.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            writeJson(res, Result.error(401, "未登录或token缺失"));
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (!jwtUtil.validateToken(token)) {
            writeJson(res, Result.error(401, "token无效或已过期"));
            return;
        }

        // 将用户信息注入 ThreadLocal 上下文
        UserContext.set(jwtUtil.getUserId(token), jwtUtil.getUsername(token));

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /** 将错误结果以 JSON 形式写回响应 */
    private void writeJson(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
