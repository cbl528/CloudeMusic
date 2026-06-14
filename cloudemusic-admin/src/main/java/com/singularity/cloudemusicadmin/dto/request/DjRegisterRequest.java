package com.singularity.cloudemusicadmin.dto.request;

import lombok.Data;

@Data
public class DjRegisterRequest {
    private Long songId;
    private String songName = "";
    private String songArtists = "";
}
