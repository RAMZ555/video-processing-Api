package com.videoprocessing.dtos.requestDtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddWatermarkRequest {
    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotBlank(message = "Watermark image path is required")
    private String watermarkPath;

    @Builder.Default
    private Integer positionX = 10;
    @Builder.Default
    private Integer positionY = 10;

    @DecimalMin(value = "0.1", message = "Opacity must be between 0.1 and 1.0")
    @DecimalMax(value = "1.0", message = "Opacity must be between 0.1 and 1.0")
    @Builder.Default
    private Double opacity = 0.7;

    @Builder.Default
    private String position = "top-right"; // top-left, top-right, bottom-left, bottom-right, center
}
