package com.singularity.cloudemusicadmin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://127.0.0.1:8001}")
    private String aiServiceUrl;

    /**
     * 调用 FastAPI AI 服务生成智能歌单。
     *
     * @param keyword   用户输入的心情/场景关键词
     * @param favorites 用户收藏歌曲列表（可为空）
     * @return FastAPI 返回的原始 JSON
     */
    public JsonNode generatePlaylist(String keyword, List<?> favorites) {
        String url = aiServiceUrl + "/ai/playlist/generate";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("keyword", keyword);
        requestBody.set("favorites", objectMapper.valueToTree(favorites));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        log.info("Calling AI service: keyword={}, favoritesCount={}", keyword, favorites.size());
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                url, request, JsonNode.class);
        return response.getBody();
    }
}
