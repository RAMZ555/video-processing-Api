package com.videoprocessing.dtos.responseDtos;


import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private Long id;
    private String uuid;
    private String filename;
    private String originalFilename;
    private Double duration;
    private Long size;
    private Integer width;
    private Integer height;
    private LocalDateTime uploadTime;
}