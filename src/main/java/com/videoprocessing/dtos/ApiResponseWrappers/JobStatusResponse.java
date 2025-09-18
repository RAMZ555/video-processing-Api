package com.videoprocessing.dtos.ApiResponseWrappers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {
    private String jobId;
    private String status;
    private Integer progress; // 0-100
    private String message;
    private String resultUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
