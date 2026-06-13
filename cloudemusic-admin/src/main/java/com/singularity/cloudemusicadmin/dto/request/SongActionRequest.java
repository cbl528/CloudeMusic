package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;

@Data
public class SongActionRequest {
    private Long songId;
    private String songName;
    private String artist;
    private String cover;
    private Long duration;
}
