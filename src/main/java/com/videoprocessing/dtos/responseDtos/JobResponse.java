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
public class JobResponse {
    private String jobId;
    private String jobType;
    private String status;
    private String resultPath;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
