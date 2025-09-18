package com.videoprocessing.dtos.requestDtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQualitiesRequest {
    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotEmpty(message = "At least one quality must be specified")
    private List<@Pattern(regexp = "480p|720p|1080p", message = "Quality must be 480p, 720p, or 1080p") String> qualities;
}
