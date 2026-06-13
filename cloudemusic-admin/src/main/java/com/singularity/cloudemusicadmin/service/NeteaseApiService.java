package com.singularity.cloudemusicadmin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.cloudemusicadmin.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class NeteaseApiService {

    private final RestTemplate restTemplate;

    @Value("${netease.api.base-url:http://127.0.0.1:3000}")
    private String baseUrl;

    /** 搜索 */
    public JsonNode search(String keywords, int type, int offset, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("keywords", keywords)
                .queryParam("type", type)
                .queryParam("offset", offset)
                .queryParam("limit", limit)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 获取歌曲播放链接 */
    public JsonNode songUrl(String ids, Integer br) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/song/url")
                .queryParam("id", ids)
                .queryParam("br", br != null ? br : 320000)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 获取歌词 */
    public JsonNode lyric(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/lyric")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 首页 Banner */
    public JsonNode banner() {
        return callApi(baseUrl + "/banner");
    }

    /** 推荐歌单 */
    public JsonNode personalized(int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/personalized")
                .queryParam("limit", limit)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 推荐新音乐 */
    public JsonNode newSong(int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/personalized/newsong")
                .queryParam("limit", limit)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 所有榜单 */
    public JsonNode toplist() {
        return callApi(baseUrl + "/toplist");
    }

    /** 榜单详情（含歌曲列表） */
    public JsonNode topList(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/top/list")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 热门歌手 */
    public JsonNode topArtists(int limit, int offset) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/top/artists")
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 歌曲详情 */
    public JsonNode songDetail(String ids) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/song/detail")
                .queryParam("ids", ids)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 歌单详情 */
    public JsonNode playlistDetail(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/playlist/detail")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 热搜列表 */
    public JsonNode searchHot() {
        return callApi(baseUrl + "/search/hot");
    }

    /** 分类歌手列表 */
    public JsonNode artistList(String cat, int limit, int offset) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/artist/list")
                .queryParam("cat", cat)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 歌手详情 + 热门50首 */
    public JsonNode artists(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/artists")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 歌手专辑列表 */
    public JsonNode artistAlbum(long id, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/artist/album")
                .queryParam("id", id)
                .queryParam("limit", limit)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 歌手描述/介绍 */
    public JsonNode artistDesc(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/artist/desc")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 专辑详情（含歌曲列表） */
    public JsonNode album(long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/album")
                .queryParam("id", id)
                .build().encode().toUri();
        return callApi(uri);
    }

    /** 统一调用入口：检查 code 并返回完整响应体 */
    private JsonNode callApi(String url) {
        return callApi(URI.create(url));
    }

    private JsonNode callApi(URI uri) {
        log.debug("Netease API call: {}", uri);
        try {
            JsonNode resp = restTemplate.getForObject(uri, JsonNode.class);
            if (resp == null) {
                throw new BusinessException(502, "网易云 API 无响应");
            }
            int code = resp.path("code").asInt(0);
            if (code != 200) {
                throw new BusinessException(502, "网易云 API 返回错误: " + code);
            }
            return resp;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用网易云 API 失败: {}", uri, e);
            throw new BusinessException(502, "调用网易云 API 失败: " + e.getMessage());
        }
    }
}
