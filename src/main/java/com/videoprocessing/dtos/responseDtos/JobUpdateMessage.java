package com.videoprocessing.dtos.responseDtos;

@lombok.Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class JobUpdateMessage {
    private String jobId;
    private String status;
    private Integer progress;
}

