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
public class QualityVersionResponse {
    private String quality;
    private String filename;
    private Long size;
    private LocalDateTime createdAt;
    private String downloadUrl;
}
