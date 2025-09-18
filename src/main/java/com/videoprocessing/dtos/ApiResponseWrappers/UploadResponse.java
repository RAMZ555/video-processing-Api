package com.videoprocessing.dtos.ApiResponseWrappers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String videoId;
    private String jobId;
    private String message;
    private String status;
}
