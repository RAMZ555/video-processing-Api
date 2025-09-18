package com.videoprocessing.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.HttpURLConnection;

@Service
@Component
@Slf4j
public class FontService {

    @Value("${app.fonts.dir:./fonts}")
    private String fontsDir;

    // Updated with working Google Fonts URLs (using direct download URLs)
    private static final Map<String, FontConfig> FONT_CONFIGS = Map.of(
            "hi", new FontConfig("NotoSansDevanagari-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansDevanagari/NotoSansDevanagari-Regular.ttf"),
            "ta", new FontConfig("NotoSansTamil-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansTamil/NotoSansTamil-Regular.ttf"),
            "te", new FontConfig("NotoSansTelugu-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansTelugu/NotoSansTelugu-Regular.ttf"),
            "bn", new FontConfig("NotoSansBengali-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansBengali/NotoSansBengali-Regular.ttf"),
            "gu", new FontConfig("NotoSansGujarati-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansGujarati/NotoSansGujarati-Regular.ttf"),
            "kn", new FontConfig("NotoSansKannada-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansKannada/NotoSansKannada-Regular.ttf"),
            "ml", new FontConfig("NotoSansMalayalam-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansMalayalam/NotoSansMalayalam-Regular.ttf"),
            "pa", new FontConfig("NotoSansGurmukhi-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansGurmukhi/NotoSansGurmukhi-Regular.ttf"),
            "mr", new FontConfig("NotoSansDevanagari-Regular.ttf", "https://github.com/googlefonts/noto-fonts/raw/main/hinted/ttf/NotoSansDevanagari/NotoSansDevanagari-Regular.ttf")
    );

    @PostConstruct
    public void initializeFonts() {
        try {
            // Create fonts directory
            Path fontsDirPath = Paths.get(fontsDir);
            Files.createDirectories(fontsDirPath);

            log.info("Initializing fonts in directory: {}", fontsDirPath.toAbsolutePath());

            int successCount = 0;
            int totalFonts = FONT_CONFIGS.size();

            // Download missing fonts with individual error handling
            for (Map.Entry<String, FontConfig> entry : FONT_CONFIGS.entrySet()) {
                String language = entry.getKey();
                FontConfig config = entry.getValue();
                Path fontFile = fontsDirPath.resolve(config.filename);

                try {
                    if (!Files.exists(fontFile)) {
                        log.info("Downloading font for {}: {}", language, config.filename);
                        if (downloadFontWithRetry(config.url, fontFile, 3)) {
                            successCount++;
                            log.info("Successfully downloaded font for {}", language);
                        } else {
                            log.warn("Failed to download font for {} after retries, will use fallback", language);
                        }
                    } else {
                        log.debug("Font already exists: {}", fontFile);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("Error downloading font for language {}: {}. Will use fallback font.", language, e.getMessage());
                }
            }

            log.info("Font initialization completed: {}/{} fonts available", successCount, totalFonts);

            // Don't fail the application even if some/all fonts failed to download
            if (successCount == 0) {
                log.warn("No custom fonts were downloaded, will rely on system fallback fonts");
            }

        } catch (Exception e) {
            log.error("Error during font initialization, continuing with fallback fonts", e);
            // Don't throw exception - let the app start with fallback fonts
        }
    }

    public String getFontPathForLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return getDefaultFontPath();
        }

        FontConfig config = FONT_CONFIGS.get(language.toLowerCase().trim());

        if (config == null) {
            log.debug("No font configuration for language: {}, using default", language);
            return getDefaultFontPath();
        }

        Path fontPath = Paths.get(fontsDir, config.filename);

        if (!Files.exists(fontPath)) {
            log.debug("Custom font file not found: {}, trying system fallback", fontPath);
            return getSystemFallbackFont(language);
        }

        String absolutePath = fontPath.toAbsolutePath().toString();
        // Convert Windows paths to FFmpeg-compatible format
        return absolutePath.replace("\\", "/");
    }

    private boolean downloadFontWithRetry(String url, Path targetFile, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(30000); // 30 seconds
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = connection.getInputStream()) {
                        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        log.debug("Downloaded font: {} (attempt {})", targetFile.getFileName(), attempt);
                        return true;
                    }
                } else {
                    log.warn("HTTP {} for font download: {} (attempt {})", responseCode, url, attempt);
                }

            } catch (IOException e) {
                log.warn("Font download attempt {} failed for {}: {}", attempt, url, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Progressive delay
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return false;
    }

    private String getSystemFallbackFont(String language) {
        // Windows system fonts as fallback
        String systemFont = switch (language.toLowerCase()) {
            case "hi", "mr" -> "C:/Windows/Fonts/mangal.ttf";
            case "ta" -> "C:/Windows/Fonts/latha.ttf";
            case "te" -> "C:/Windows/Fonts/gautami.ttf";
            case "bn" -> "C:/Windows/Fonts/vrinda.ttf";
            case "gu" -> "C:/Windows/Fonts/shruti.ttf";
            case "kn" -> "C:/Windows/Fonts/tunga.ttf";
            case "ml" -> "C:/Windows/Fonts/kartika.ttf";
            case "pa" -> "C:/Windows/Fonts/raavi.ttf";
            default -> "C:/Windows/Fonts/arial.ttf";
        };

        if (new File(systemFont).exists()) {
            log.debug("Using system font: {}", systemFont);
            return systemFont.replace("\\", "/");
        }

        log.debug("System font not found: {}, using default", systemFont);
        return getDefaultFontPath();
    }

    private String getDefaultFontPath() {
        // Try common default fonts across different operating systems
        String[] defaultFonts = {
                "C:/Windows/Fonts/arial.ttf",        // Windows
                "C:/Windows/Fonts/calibri.ttf",      // Windows
                "/System/Library/Fonts/Arial.ttf",   // macOS
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", // Linux
                "/usr/share/fonts/TTF/DejaVuSans.ttf" // Some Linux distributions
        };

        for (String font : defaultFonts) {
            if (new File(font).exists()) {
                log.debug("Using default font: {}", font);
                return font.replace("\\", "/");
            }
        }

        // If no fonts found, return empty (FFmpeg will use its default)
        log.debug("No default fonts found, FFmpeg will use system default");
        return "";
    }

    // Add method to check if font service is working
    public boolean isFontAvailable(String language) {
        String fontPath = getFontPathForLanguage(language);
        return fontPath != null && !fontPath.isEmpty() && new File(fontPath.replace("/", "\\")).exists();
    }

    @Data
    @AllArgsConstructor
    private static class FontConfig {
        private final String filename;
        private final String url;
    }
}

