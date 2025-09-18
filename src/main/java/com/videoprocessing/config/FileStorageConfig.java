package com.videoprocessing.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FileStorageConfig {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.output.dir:./outputs}")
    private String outputDir;

    @Value("${app.fonts.dir:./fonts}")
    private String fontsDir;

    @PostConstruct
    public void init() {
        createDirectoryIfNotExists(uploadDir, "Upload");
        createDirectoryIfNotExists(outputDir, "Output");
        createDirectoryIfNotExists(fontsDir, "Fonts");
    }

    private void createDirectoryIfNotExists(String dir, String type) {
        try {
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("{} directory created: {}", type, path.toAbsolutePath());
            } else {
                log.info("{} directory exists: {}", type, path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to create {} directory: {}", type, dir, e);
            throw new RuntimeException("Cannot create " + type + " directory", e);
        }
    }
}
