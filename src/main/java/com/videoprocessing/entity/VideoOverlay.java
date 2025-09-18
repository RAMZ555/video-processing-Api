package com.videoprocessing.entity;


import com.videoprocessing.enums.OverlayType;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "video_overlays")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    @ToString.Exclude
    private Video video;

    @Enumerated(EnumType.STRING)
    private OverlayType overlayType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private Integer positionX = 0;
    @Builder.Default
    private Integer positionY = 0;
    @Builder.Default
    private Double startTime = 0.0;

    private Double endTime;

    @Builder.Default
    private Integer fontSize = 24;
    @Builder.Default
    private String fontColor = "white";
    @Builder.Default
    private String language = "en";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
