package com.videoprocessing.entity;

import com.videoprocessing.enums.JobStatus;
import com.videoprocessing.enums.JobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String jobId;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    @ToString.Exclude
    private Video video;

    private String resultPath;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}

