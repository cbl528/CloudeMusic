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

        // 登录和注册接口放行
        if (path.equals(LOGIN_PATH) || path.equals(REGISTER_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        // 只拦截 /api/cloude/music/user/ 开头的请求
        if (!path.startsWith("/api/cloude/music/user/")) {
            chain.doFilter(request, response);
            return;
        }

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

        req.setAttribute("userId", jwtUtil.getUserId(token));
        req.setAttribute("username", jwtUtil.getUsername(token));

        chain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
