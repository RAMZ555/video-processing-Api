package com.videoprocessing.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uuid;

    private String filename;
    private String originalFilename;
    private String filepath;
    private Double duration;
    private Long size;
    private Integer width;
    private Integer height;

    @Builder.Default
    private LocalDateTime uploadTime = LocalDateTime.now();

    @OneToMany(mappedBy = "originalVideo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TrimmedVideo> trimmedVideos;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<VideoOverlay> overlays;

    @OneToMany(mappedBy = "originalVideo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<VideoQualityVersion> qualityVersions;
}
