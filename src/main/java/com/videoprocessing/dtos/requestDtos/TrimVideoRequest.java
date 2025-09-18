package com.videoprocessing.dtos.requestDtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrimVideoRequest {
    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotNull(message = "Start time is required")
    @DecimalMin(value = "0.0", message = "Start time must be positive")
    private Double startTime;

    @NotNull(message = "End time is required")
    @DecimalMin(value = "0.1", message = "End time must be greater than 0.1")
    private Double endTime;

    @AssertTrue(message = "End time must be greater than start time")
    public boolean isValidTimeRange() {
        return endTime != null && startTime != null && endTime > startTime;
    }
}
