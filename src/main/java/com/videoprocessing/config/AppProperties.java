package com.videoprocessing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppProperties {

    @NotBlank
    private String uploadDir = "./uploads";

    @NotBlank
    private String outputDir = "./outputs";

    @NotBlank
    private String fontsDir = "./fonts";

    @NotBlank
    private String ffmpegPath = "ffmpeg";

    @Positive
    private long maxFileSize = 524288000L; // 500MB

    @Positive
    private int maxConcurrentJobs = 10;

    private boolean cleanupEnabled = true;

    @Positive
    private int cleanupRetentionDays = 7;
}
