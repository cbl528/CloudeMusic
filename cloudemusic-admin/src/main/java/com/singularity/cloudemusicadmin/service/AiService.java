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

    private JsonNode callAiService(String path, Map<String, Object> body) {
        String urlStr = aiServiceUrl + path;
        try {
            byte[] jsonBytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            log.info("Calling AI service: POST {} body={}", path, new String(jsonBytes));

            URI uri = new URI(urlStr);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(45000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBytes);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
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
            log.error("Failed to call AI service: POST {}", path, e);
            throw new RuntimeException("调用 AI 服务失败: " + e.getMessage(), e);
        }
    }

    public JsonNode generatePlaylist(String keyword, List<?> favorites) {
        Map<String, Object> body = new HashMap<>();
        body.put("keyword", keyword);
        body.put("favorites", favorites);
        return callAiService("/ai/playlist/generate", body);
    }

    public JsonNode djRegister(Long songId, String songName, String songArtists) {
        Map<String, Object> body = new HashMap<>();
        body.put("song_id", songId);
        body.put("song_name", songName);
        body.put("song_artists", songArtists);
        return callAiService("/ai/dj/register", body);
    }

    public JsonNode djRecommend(Long currentSongId, String currentSongName,
                                 String currentSongArtists, List<Long> recentIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("current_song_id", currentSongId);
        body.put("current_song_name", currentSongName);
        body.put("current_song_artists", currentSongArtists);
        body.put("recent_ids", recentIds);
        return callAiService("/ai/dj/recommend", body);
    }
}
