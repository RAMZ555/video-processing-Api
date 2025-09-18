package com.videoprocessing.entity;

import com.videoprocessing.enums.VideoQuality;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_quality_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoQualityVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_video_id")
    @ToString.Exclude
    private Video originalVideo;

    @Enumerated(EnumType.STRING)
    private VideoQuality quality;

    private String filename;
    private String filepath;
    private Long size;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
