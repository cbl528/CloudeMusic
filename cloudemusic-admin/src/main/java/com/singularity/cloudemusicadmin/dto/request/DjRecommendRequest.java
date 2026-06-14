package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DjRecommendRequest {
    private Long currentSongId;
    private String currentSongName = "";
    private String currentSongArtists = "";
    private List<Long> recentIds = List.of();
}
