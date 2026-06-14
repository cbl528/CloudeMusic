package com.singularity.cloudemusicadmin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://127.0.0.1:8001}")
    private String aiServiceUrl;

    public JsonNode generatePlaylist(String keyword, List<?> favorites) {
        String urlStr = aiServiceUrl + "/ai/playlist/generate";

        try {
            // 构建请求体 JSON
            Map<String, Object> body = new HashMap<>();
            body.put("keyword", keyword);
            body.put("favorites", favorites);
            byte[] jsonBytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);

            log.info("Calling AI service: keyword={}, favoritesCount={}, body={}",
                    keyword, favorites.size(), new String(jsonBytes));

            // 用 HttpURLConnection 发送 POST 请求
            URI uri = new URI(urlStr);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(45000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBytes);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            log.info("AI service response code: {}", responseCode);

            // 读取响应
            byte[] responseBytes;
            if (responseCode >= 200 && responseCode < 300) {
                responseBytes = conn.getInputStream().readAllBytes();
            } else {
                responseBytes = conn.getErrorStream().readAllBytes();
                String errorBody = new String(responseBytes, StandardCharsets.UTF_8);
                log.error("AI service error response: {}", errorBody);
                throw new RuntimeException("AI 服务返回错误 " + responseCode + ": " + errorBody);
            }

            conn.disconnect();
            return objectMapper.readTree(responseBytes);
        } catch (Exception e) {
            log.error("Failed to call AI service", e);
            throw new RuntimeException("调用 AI 服务失败: " + e.getMessage(), e);
        }
    }
}
