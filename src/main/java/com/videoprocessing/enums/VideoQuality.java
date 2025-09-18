package com.videoprocessing.enums;

import lombok.Getter;

public enum VideoQuality {
    QUALITY_480P("480p", 640, 480),
    QUALITY_720P("720p", 1280, 720),
    QUALITY_1080P("1080p", 1920, 1080);

    @Getter
    private final String resolution;
    @Getter
    private final int width;
    @Getter
    private final int height;

    VideoQuality(String resolution, int width, int height) {
        this.resolution = resolution;
        this.width = width;
        this.height = height;
    }
}
