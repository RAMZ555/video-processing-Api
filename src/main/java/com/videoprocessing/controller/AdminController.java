package com.videoprocessing.controller;

import com.videoprocessing.dtos.ApiResponseWrappers.ApiResponse;
import com.videoprocessing.dtos.SystemStats;
import com.videoprocessing.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final VideoProcessingService videoService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SystemStats>> getSystemStats() {
        // Implementation would fetch actual system stats from service/db
        SystemStats stats = SystemStats.builder()
                .totalVideos(0L)
                .totalJobs(0L)
                .completedJobs(0L)
                .failedJobs(0L)
                .storageUsed(0L)
                .build();

        return ResponseEntity.ok(ApiResponse.success("System stats", stats));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupOldFiles() {
        // Implementation would cleanup old processed files
        return ResponseEntity.ok(ApiResponse.success("Cleanup initiated"));
    }
}
