package com.videoprocessing.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoListResponse {
    private List<VideoResponse> videos;
    private int totalCount;
    private int page;
    private int size;
}
