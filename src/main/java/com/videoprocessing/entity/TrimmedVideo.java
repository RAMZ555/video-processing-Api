package com.videoprocessing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trimmed_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrimmedVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_video_id")
    @ToString.Exclude
    private Video originalVideo;

    private String filename;
    private String filepath;
    private Double startTime;
    private Double endTime;
    private Double duration;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
