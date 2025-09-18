package com.videoprocessing.controller;

import com.videoprocessing.dtos.requestDtos.*;
import com.videoprocessing.dtos.responseDtos.*;
import com.videoprocessing.dtos.ApiResponseWrappers.*;
import com.videoprocessing.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VideoController {

    private final VideoProcessingService videoService;

    // ===== LEVEL 1: UPLOAD & METADATA =====

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponse>> uploadVideo(
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading video: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        if (!isVideoFile(file)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be a video"));
        }

        UploadResponse response = videoService.uploadVideo(file);
        return ResponseEntity.ok(ApiResponse.success("Video upload started", response));
    }

    @GetMapping("/videos")
    public ResponseEntity<ApiResponse<List<VideoResponse>>> getAllVideos() {
        List<VideoResponse> videos = videoService.getAllVideos();
        return ResponseEntity.ok(ApiResponse.success("Videos retrieved successfully", videos));
    }

    @GetMapping("/videos/{videoId}")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideo(@PathVariable Long videoId) {
        // Implementation would fetch single video by ID
        return ResponseEntity.ok(ApiResponse.success("Video found", null));
    }

    // ===== LEVEL 2: TRIMMING API =====

    @PostMapping("/trim")
    public ResponseEntity<ApiResponse<JobResponse>> trimVideo(@Valid @RequestBody TrimVideoRequest request) {
        log.info("Trimming video ID: {} from {}s to {}s",
                request.getVideoId(), request.getStartTime(), request.getEndTime());

        JobResponse response = videoService.trimVideo(request);
        return ResponseEntity.ok(ApiResponse.success("Trimming started", response));
    }

    // ===== LEVEL 3: OVERLAYS & WATERMARKING =====

    @PostMapping("/overlay")
    public ResponseEntity<ApiResponse<JobResponse>> addOverlay(@Valid @RequestBody AddOverlayRequest request) {
        log.info("Adding {} overlay to video ID: {}", request.getOverlayType(), request.getVideoId());

        JobResponse response = videoService.addOverlay(request);
        return ResponseEntity.ok(ApiResponse.success("Overlay processing started", response));
    }

    @PostMapping("/watermark")
    public ResponseEntity<ApiResponse<JobResponse>> addWatermark(@Valid @RequestBody AddWatermarkRequest request) {
        log.info("Adding watermark to video ID: {}", request.getVideoId());

        JobResponse response = videoService.addWatermark(request);
        return ResponseEntity.ok(ApiResponse.success("Watermark processing started", response));
    }

    // ===== LEVEL 4: ASYNC JOB QUEUE =====

    @GetMapping("/status/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(@PathVariable String jobId) {
        JobStatusResponse response = videoService.getJobStatus(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job status retrieved", response));
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<Resource> downloadJobResult(@PathVariable String jobId) {
        try {
            String filePath = videoService.getJobResult(jobId);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading job result: {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== LEVEL 5: MULTIPLE OUTPUT QUALITIES =====

    @PostMapping("/qualities")
    public ResponseEntity<ApiResponse<JobResponse>> generateMultipleQualities(
            @Valid @RequestBody GenerateQualitiesRequest request) {

        log.info("Generating qualities {} for video ID: {}",
                request.getQualities(), request.getVideoId());

        JobResponse response = videoService.generateMultipleQualities(request);
        return ResponseEntity.ok(ApiResponse.success("Quality conversion started", response));
    }

    @GetMapping("/videos/{videoId}/qualities")
    public ResponseEntity<ApiResponse<List<QualityVersionResponse>>> getVideoQualities(@PathVariable Long videoId) {
        List<QualityVersionResponse> qualities = videoService.getVideoQualityVersions(videoId);
        return ResponseEntity.ok(ApiResponse.success("Quality versions retrieved", qualities));
    }

    @GetMapping("/download/quality/{qualityId}")
    public ResponseEntity<Resource> downloadQualityVersion(@PathVariable Long qualityId) {
        // Implementation would download specific quality version
        return ResponseEntity.ok().build();
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service is healthy"));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        // Implementation would fetch paginated jobs
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved", List.of()));
    }

    // ===== PRIVATE HELPER METHODS =====

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("video/");
    }

    // ===== EXCEPTION HANDLER =====

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred", e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
