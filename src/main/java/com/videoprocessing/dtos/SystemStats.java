package com.videoprocessing.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStats {
    private Long totalVideos;
    private Long totalJobs;
    private Long completedJobs;
    private Long failedJobs;
    private Long storageUsed;
}

