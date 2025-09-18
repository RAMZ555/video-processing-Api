package com.videoprocessing.service;

import com.videoprocessing.entity.*;
import com.videoprocessing.enums.*;
import com.videoprocessing.repository.*;
import com.videoprocessing.dtos.requestDtos.*;
import com.videoprocessing.dtos.responseDtos.*;
import com.videoprocessing.dtos.ApiResponseWrappers.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoProcessingService {

    private final VideoRepository videoRepository;
    private final ProcessingJobRepository jobRepository;
    private final TrimmedVideoRepository trimmedVideoRepository;
    private final VideoOverlayRepository overlayRepository;
    private final VideoQualityVersionRepositor qualityRepository;
    private final FontService fontService; // Add this dependency

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.output.dir:./outputs}")
    private String outputDir;

    // ===== LEVEL 1: UPLOAD & METADATA =====

    public UploadResponse uploadVideo(MultipartFile file) {
        String jobId = UUID.randomUUID().toString();

        // Create job immediately
        ProcessingJob job = ProcessingJob.builder()
                .jobId(jobId)
                .jobType(JobType.UPLOAD)
                .status(JobStatus.PENDING)
                .build();
        jobRepository.save(job);

        // Process async
        processVideoUpload(file, jobId);

        return UploadResponse.builder()
                .jobId(jobId)
                .message("Upload started, processing in background")
                .status("PENDING")
                .build();
    }

    @Async
    public CompletableFuture<Void> processVideoUpload(MultipartFile file, String jobId) {
        try {
            ProcessingJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            // Save file
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Get video metadata using FFmpeg
            VideoMetadata metadata = getVideoMetadata(filePath.toString());

            // Save to database
            Video video = Video.builder()
                    .uuid(UUID.randomUUID().toString())
                    .filename(filename)
                    .originalFilename(file.getOriginalFilename())
                    .filepath(filePath.toString())
                    .duration(metadata.getDuration())
                    .size(file.getSize())
                    .width(metadata.getWidth())
                    .height(metadata.getHeight())
                    .build();

            videoRepository.save(video);

            // Update job
            job.setStatus(JobStatus.COMPLETED);
            job.setVideo(video);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Video upload completed: {}", video.getUuid());

        } catch (Exception e) {
            log.error("Video upload failed for job: {}", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    public List<VideoResponse> getAllVideos() {
        return videoRepository.findByOrderByUploadTimeDesc()
                .stream()
                .map(this::mapToVideoResponse)
                .toList();
    }
    public List<ProcessingJob> getFailedJobsInLast24Hours() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        return jobRepository.findRecentFailedJobs(cutoff);
    }


    // ===== LEVEL 2: TRIMMING API =====

    public JobResponse trimVideo(TrimVideoRequest request) {
        String jobId = UUID.randomUUID().toString();
        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        ProcessingJob job = ProcessingJob.builder()
                .jobId(jobId)
                .jobType(JobType.TRIM)
                .status(JobStatus.PENDING)
                .video(video)
                .build();
        jobRepository.save(job);

        // Process async
        processTrimVideo(request, jobId);

        return JobResponse.builder()
                .jobId(jobId)
                .jobType("TRIM")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Async
    public CompletableFuture<Void> processTrimVideo(TrimVideoRequest request, String jobId) {
        try {
            ProcessingJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            Video originalVideo = job.getVideo();
            String outputFilename = "trimmed_" + UUID.randomUUID() + ".mp4";
            String outputPath = Paths.get(outputDir, outputFilename).toString();

            // FFmpeg trim command
            String[] command = {
                    "ffmpeg", "-i", originalVideo.getFilepath(),
                    "-ss", String.valueOf(request.getStartTime()),
                    "-t", String.valueOf(request.getEndTime() - request.getStartTime()),
                    "-c", "copy", "-avoid_negative_ts", "make_zero",
                    outputPath
            };

            executeFFmpegCommand(command);

            // Save trimmed video record
            TrimmedVideo trimmedVideo = TrimmedVideo.builder()
                    .uuid(UUID.randomUUID().toString())
                    .originalVideo(originalVideo)
                    .filename(outputFilename)
                    .filepath(outputPath)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .duration(request.getEndTime() - request.getStartTime())
                    .build();

            trimmedVideoRepository.save(trimmedVideo);

            // Update job
            job.setStatus(JobStatus.COMPLETED);
            job.setResultPath(outputPath);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Video trimming completed: {}", trimmedVideo.getUuid());

        } catch (Exception e) {
            log.error("Video trimming failed for job: {}", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    // ===== LEVEL 3: OVERLAYS & WATERMARKING =====

    public JobResponse addOverlay(AddOverlayRequest request) {
        String jobId = UUID.randomUUID().toString();
        log.info("Adding {} overlay to video ID: {}", request.getOverlayType(), request.getVideoId());

        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        ProcessingJob job = ProcessingJob.builder()
                .jobId(jobId)
                .jobType(JobType.OVERLAY)
                .status(JobStatus.PENDING)
                .video(video)
                .build();
        jobRepository.save(job);

        // Process async
        processAddOverlay(request, jobId);

        return JobResponse.builder()
                .jobId(jobId)
                .jobType("OVERLAY")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Async
    public CompletableFuture<Void> processAddOverlay(AddOverlayRequest request, String jobId) {
        try {
            ProcessingJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            Video video = job.getVideo();
            String outputFilename = "overlay_" + UUID.randomUUID() + ".mp4";
            String outputPath = Paths.get(outputDir, outputFilename).toString();

            // Build FFmpeg command based on overlay type
            List<String> command = new ArrayList<>();
            command.addAll(Arrays.asList("ffmpeg", "-i", video.getFilepath()));

            if ("TEXT".equals(request.getOverlayType())) {
                // Use FontService to get proper font path
                String fontPath = fontService.getFontPathForLanguage(request.getLanguage());

                // Build the drawtext filter with proper escaping
                StringBuilder filterBuilder = new StringBuilder("drawtext=");

                // Escape text for FFmpeg - handle Unicode properly
                String escapedText = request.getContent()
                        .replace("\\", "\\\\")    // Escape backslashes
                        .replace(":", "\\:")      // Escape colons
                        .replace("'", "\\'");     // Escape single quotes

                filterBuilder.append("text='").append(escapedText).append("'");
                filterBuilder.append(":x=").append(request.getPositionX());
                filterBuilder.append(":y=").append(request.getPositionY());
                filterBuilder.append(":fontsize=").append(request.getFontSize());
                filterBuilder.append(":fontcolor=").append(request.getFontColor());

                // Add font file if available - fix Windows path escaping
                if (fontPath != null && !fontPath.isEmpty()) {
                    // Escape Windows backslashes and colons in path
                    String escapedFontPath = fontPath
                            .replace("\\", "\\\\")
                            .replace(":", "\\:");
                    filterBuilder.append(":fontfile='").append(escapedFontPath).append("'");
                    log.info("Using font: {} for language: {}", fontPath, request.getLanguage());
                }

                // Add time constraints if specified - fix the enable syntax
                if (request.getStartTime() > 0 || request.getEndTime() != null) {
                    double endTime = request.getEndTime() != null ? request.getEndTime() : 999999.0;
                    filterBuilder.append(":enable='between(t\\,")
                            .append(String.format("%.2f", request.getStartTime()))
                            .append("\\,")
                            .append(String.format("%.2f", endTime))
                            .append(")'");
                }

                command.addAll(Arrays.asList("-vf", filterBuilder.toString()));

            } else if ("IMAGE".equals(request.getOverlayType())) {
                command.addAll(Arrays.asList("-i", request.getContent()));
                String filterComplex = String.format(
                        "[1:v]scale=200:200[overlay];[0:v][overlay]overlay=%d:%d",
                        request.getPositionX(),
                        request.getPositionY()
                );
                command.addAll(Arrays.asList("-filter_complex", filterComplex));
            }

            command.addAll(Arrays.asList("-c:a", "copy", outputPath));

            // Log the exact command being executed
            log.info("Executing FFmpeg command: {}", String.join(" ", command));

            executeFFmpegCommand(command.toArray(new String[0]));

            // Save overlay record
            VideoOverlay overlay = VideoOverlay.builder()
                    .video(video)
                    .overlayType(OverlayType.valueOf(request.getOverlayType()))
                    .content(request.getContent())
                    .positionX(request.getPositionX())
                    .positionY(request.getPositionY())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .fontSize(request.getFontSize())
                    .fontColor(request.getFontColor())
                    .language(request.getLanguage())
                    .build();

            overlayRepository.save(overlay);

            // Update job
            job.setStatus(JobStatus.COMPLETED);
            job.setResultPath(outputPath);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Overlay processing completed for video: {}", video.getUuid());

        } catch (Exception e) {
            log.error("Overlay processing failed for job: {}", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    public JobResponse addWatermark(AddWatermarkRequest request) {
        String jobId = UUID.randomUUID().toString();
        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        ProcessingJob job = ProcessingJob.builder()
                .jobId(jobId)
                .jobType(JobType.WATERMARK)
                .status(JobStatus.PENDING)
                .video(video)
                .build();
        jobRepository.save(job);

        // Process async
        processAddWatermark(request, jobId);

        return JobResponse.builder()
                .jobId(jobId)
                .jobType("WATERMARK")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Async
    public CompletableFuture<Void> processAddWatermark(AddWatermarkRequest request, String jobId) {
        try {
            ProcessingJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            Video video = job.getVideo();
            String outputFilename = "watermark_" + UUID.randomUUID() + ".mp4";
            String outputPath = Paths.get(outputDir, outputFilename).toString();

            // Validate watermark file exists
            if (request.getWatermarkPath() == null || request.getWatermarkPath().equals("string") || request.getWatermarkPath().trim().isEmpty()) {
                throw new RuntimeException("Invalid watermark path. Please provide a valid path to an image file (PNG, JPG, etc.)");
            }

            // Check if watermark file exists
            Path watermarkPath = Paths.get(request.getWatermarkPath());
            if (!Files.exists(watermarkPath)) {
                throw new RuntimeException("Watermark file not found: " + request.getWatermarkPath());
            }

            // Calculate watermark position based on position string
            String overlayPosition = calculateWatermarkPosition(request.getPosition(),
                    video.getWidth(), video.getHeight());

            // Build FFmpeg command
            String[] command = {
                    "ffmpeg", "-i", video.getFilepath(),
                    "-i", request.getWatermarkPath(),
                    "-filter_complex",
                    String.format("[1:v]format=rgba,colorchannelmixer=aa=%f[watermark];[0:v][watermark]overlay=%s",
                            request.getOpacity(), overlayPosition),
                    "-c:a", "copy",
                    outputPath
            };

            // Log the command for debugging
            log.info("Executing watermark FFmpeg command: {}", String.join(" ", command));

            executeFFmpegCommand(command);

            // Save watermark overlay record
            VideoOverlay watermark = VideoOverlay.builder()
                    .video(video)
                    .overlayType(OverlayType.WATERMARK)
                    .content(request.getWatermarkPath())
                    .positionX(request.getPositionX())
                    .positionY(request.getPositionY())
                    .build();

            overlayRepository.save(watermark);

            // Update job
            job.setStatus(JobStatus.COMPLETED);
            job.setResultPath(outputPath);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Watermark processing completed for video: {}", video.getUuid());

        } catch (Exception e) {
            log.error("Watermark processing failed for job: {}", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    // ===== LEVEL 4: ASYNC JOB QUEUE =====

    public JobStatusResponse getJobStatus(String jobId) {
        ProcessingJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        return JobStatusResponse.builder()
                .jobId(jobId)
                .status(job.getStatus().toString())
                .progress(calculateProgress(job))
                .message(job.getErrorMessage())
                .resultUrl(job.getResultPath() != null ? "/download/" + jobId : null)
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    public String getJobResult(String jobId) {
        ProcessingJob job = jobRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new RuntimeException("Job not completed yet");
        }

        return job.getResultPath();
    }

    // ===== LEVEL 5: MULTIPLE OUTPUT QUALITIES =====

    public JobResponse generateMultipleQualities(GenerateQualitiesRequest request) {
        String jobId = UUID.randomUUID().toString();
        Video video = videoRepository.findById(request.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found"));

        ProcessingJob job = ProcessingJob.builder()
                .jobId(jobId)
                .jobType(JobType.QUALITY_CONVERSION)
                .status(JobStatus.PENDING)
                .video(video)
                .build();
        jobRepository.save(job);

        // Process async
        processQualityConversion(request, jobId);

        return JobResponse.builder()
                .jobId(jobId)
                .jobType("QUALITY_CONVERSION")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Async
    public CompletableFuture<Void> processQualityConversion(GenerateQualitiesRequest request, String jobId) {
        try {
            ProcessingJob job = jobRepository.findByJobId(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            Video video = job.getVideo();

            for (String qualityStr : request.getQualities()) {
                VideoQuality quality = VideoQuality.valueOf("QUALITY_" + qualityStr.toUpperCase());

                String outputFilename = String.format("%s_%s_%s.mp4",
                        video.getUuid(),
                        quality.getResolution(),
                        UUID.randomUUID().toString().substring(0, 8));
                String outputPath = Paths.get(outputDir, outputFilename).toString();

                // FFmpeg quality conversion command
                String[] command = {
                        "ffmpeg", "-i", video.getFilepath(),
                        "-vf", String.format("scale=%d:%d", quality.getWidth(), quality.getHeight()),
                        "-c:v", "libx264",
                        "-crf", "23",
                        "-preset", "medium",
                        "-c:a", "aac",
                        "-b:a", "128k",
                        outputPath
                };

                executeFFmpegCommand(command);

                // Get file size
                long fileSize = Files.size(Paths.get(outputPath));

                // Save quality version record
                VideoQualityVersion qualityVersion = VideoQualityVersion.builder()
                        .originalVideo(video)
                        .quality(quality)
                        .filename(outputFilename)
                        .filepath(outputPath)
                        .size(fileSize)
                        .build();

                qualityRepository.save(qualityVersion);

                log.info("Generated {} quality for video: {}", quality.getResolution(), video.getUuid());
            }

            // Update job
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Quality conversion completed for video: {}", video.getUuid());

        } catch (Exception e) {
            log.error("Quality conversion failed for job: {}", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    public List<QualityVersionResponse> getVideoQualityVersions(Long videoId) {
        return qualityRepository.findByOriginalVideoId(videoId)
                .stream()
                .map(this::mapToQualityVersionResponse)
                .toList();
    }

    // ===== UTILITY METHODS =====

    private VideoMetadata getVideoMetadata(String filePath) throws IOException, InterruptedException {
        String[] command = {
                "ffprobe", "-v", "quiet",
                "-print_format", "json",
                "-show_format", "-show_streams",
                filePath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes());
        process.waitFor();

        // Parse JSON output to extract metadata
        // This is a simplified version - you'd use Jackson or similar for real parsing
        VideoMetadata metadata = new VideoMetadata();
        if (output.contains("duration")) {
            // Extract duration, width, height from JSON
            metadata.setDuration(extractDurationFromOutput(output));
            metadata.setWidth(extractWidthFromOutput(output));
            metadata.setHeight(extractHeightFromOutput(output));
        }

        return metadata;
    }

    private void executeFFmpegCommand(String[] command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        // Set environment for better Unicode support
        Map<String, String> env = pb.environment();
        env.put("LANG", "en_US.UTF-8");

        Process process = pb.start();

        // Capture output with proper encoding
        StringBuilder outputLog = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLog.append(line).append("\n");
                log.debug("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("FFmpeg failed with exit code: {} and output: {}", exitCode, outputLog);
            throw new RuntimeException("FFmpeg command failed with exit code: " + exitCode);
        }
    }

    // Remove the old escapeTextForFFmpeg method since we're handling it differently now

    private String calculateWatermarkPosition(String position, int videoWidth, int videoHeight) {
        return switch (position.toLowerCase()) {
            case "top-left" -> "10:10";
            case "top-right" -> String.format("%d:10", videoWidth - 210);
            case "bottom-left" -> String.format("10:%d", videoHeight - 110);
            case "bottom-right" -> String.format("%d:%d", videoWidth - 210, videoHeight - 110);
            case "center" -> String.format("%d:%d", (videoWidth - 200) / 2, (videoHeight - 100) / 2);
            case "mass" -> "10:10"; // Add this line to your existing method
            default -> "10:10";
        };
    }

    private Integer calculateProgress(ProcessingJob job) {
        return switch (job.getStatus()) {
            case PENDING -> 0;
            case PROCESSING -> 50;
            case COMPLETED -> 100;
            case FAILED -> 0;
        };
    }

    private void updateJobStatus(String jobId, JobStatus status, String errorMessage) {
        jobRepository.findByJobId(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setErrorMessage(errorMessage);
            if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
                job.setCompletedAt(LocalDateTime.now());
            }
            jobRepository.save(job);
        });
    }

    private VideoResponse mapToVideoResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .uuid(video.getUuid())
                .filename(video.getFilename())
                .originalFilename(video.getOriginalFilename())
                .duration(video.getDuration())
                .size(video.getSize())
                .width(video.getWidth())
                .height(video.getHeight())
                .uploadTime(video.getUploadTime())
                .build();
    }

    private QualityVersionResponse mapToQualityVersionResponse(VideoQualityVersion version) {
        return QualityVersionResponse.builder()
                .quality(version.getQuality().getResolution())
                .filename(version.getFilename())
                .size(version.getSize())
                .createdAt(version.getCreatedAt())
                .downloadUrl("/download/quality/" + version.getId())
                .build();
    }

    // Helper methods for metadata extraction (simplified)
    private Double extractDurationFromOutput(String output) {
        // Parse JSON to extract duration - simplified version
        return 0.0; // Implement proper JSON parsing
    }

    private Integer extractWidthFromOutput(String output) {
        return 1920; // Implement proper JSON parsing

    }

    private Integer extractHeightFromOutput(String output) {
        return 1080; // Implement proper JSON parsing
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class VideoMetadata {
        private Double duration;
        private Integer width;
        private Integer height;
    }
}