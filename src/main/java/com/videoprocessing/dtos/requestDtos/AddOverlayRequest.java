package com.videoprocessing.dtos.requestDtos;

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
public class AddOverlayRequest {
    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotNull(message = "Overlay type is required")
    private String overlayType; // TEXT, IMAGE, VIDEO, WATERMARK

    @NotBlank(message = "Content is required")
    private String content;

    @Builder.Default
    private Integer positionX = 0;
    @Builder.Default
    private Integer positionY = 0;
    @Builder.Default
    private Double startTime = 0.0;

    private Double endTime;

    // Text overlay specific
    @Builder.Default
    private Integer fontSize = 24;
    @Builder.Default
    private String fontColor = "white";
    @Builder.Default
    private String language = "en"; // Support for Indian languages: hi, ta, te, bn, mr, etc.
}
