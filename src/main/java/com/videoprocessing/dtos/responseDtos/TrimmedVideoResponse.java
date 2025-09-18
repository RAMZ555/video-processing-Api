package com.videoprocessing.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrimmedVideoResponse {
    private Long id;
    private String uuid;
    private String filename;
    private Double startTime;
    private Double endTime;
    private Double duration;
    private LocalDateTime createdAt;
    private VideoResponse originalVideo;
}
