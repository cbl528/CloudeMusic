package com.singularity.cloudemusicadmin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.cloudemusicadmin.common.Result;
import com.singularity.cloudemusicadmin.service.NeteaseApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cloude/music")
@RequiredArgsConstructor
public class NeteaseController {

    private final NeteaseApiService neteaseApiService;

    @GetMapping("/search")
    public Result<JsonNode> search(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "1") int type,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "30") int limit) {
        return Result.success(neteaseApiService.search(keywords, type, offset, limit));
    }

    @GetMapping("/search/hot")
    public Result<JsonNode> searchHot() {
        return Result.success(neteaseApiService.searchHot());
    }

    @GetMapping("/song/url")
    public Result<JsonNode> songUrl(
            @RequestParam String ids,
            @RequestParam(required = false) Integer br) {
        return Result.success(neteaseApiService.songUrl(ids, br));
    }

    @GetMapping("/song/lyric")
    public Result<JsonNode> lyric(@RequestParam long id) {
        return Result.success(neteaseApiService.lyric(id));
    }

    @GetMapping("/song/detail")
    public Result<JsonNode> songDetail(@RequestParam String ids) {
        return Result.success(neteaseApiService.songDetail(ids));
    }

    @GetMapping("/banner")
    public Result<JsonNode> banner() {
        return Result.success(neteaseApiService.banner());
    }

    @GetMapping("/personalized")
    public Result<JsonNode> personalized(@RequestParam(defaultValue = "12") int limit) {
        return Result.success(neteaseApiService.personalized(limit));
    }

    @GetMapping("/personalized/newsong")
    public Result<JsonNode> newSong(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(neteaseApiService.newSong(limit));
    }

    @GetMapping("/playlist/detail")
    public Result<JsonNode> playlistDetail(@RequestParam long id) {
        return Result.success(neteaseApiService.playlistDetail(id));
    }

    @GetMapping("/toplist")
    public Result<JsonNode> toplist() {
        return Result.success(neteaseApiService.toplist());
    }

    @GetMapping("/top/list")
    public Result<JsonNode> topList(@RequestParam long id) {
        return Result.success(neteaseApiService.topList(id));
    }

    @GetMapping("/top/artists")
    public Result<JsonNode> topArtists(
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return Result.success(neteaseApiService.topArtists(limit, offset));
    }

    @GetMapping("/artist/list")
    public Result<JsonNode> artistList(
            @RequestParam String cat,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return Result.success(neteaseApiService.artistList(cat, limit, offset));
    }

    @GetMapping("/artists")
    public Result<JsonNode> artists(@RequestParam long id) {
        return Result.success(neteaseApiService.artists(id));
    }

    @GetMapping("/artist/album")
    public Result<JsonNode> artistAlbum(
            @RequestParam long id,
            @RequestParam(defaultValue = "30") int limit) {
        return Result.success(neteaseApiService.artistAlbum(id, limit));
    }

    @GetMapping("/artist/desc")
    public Result<JsonNode> artistDesc(@RequestParam long id) {
        return Result.success(neteaseApiService.artistDesc(id));
    }
}
