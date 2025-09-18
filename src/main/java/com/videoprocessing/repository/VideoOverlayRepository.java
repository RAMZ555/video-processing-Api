package com.videoprocessing.repository;


import com.videoprocessing.entity.Video;
import com.videoprocessing.entity.VideoOverlay;
import com.videoprocessing.enums.OverlayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoOverlayRepository extends JpaRepository<VideoOverlay, Long> {
    List<VideoOverlay> findByVideoId(Long videoId);

    List<VideoOverlay> findByVideoAndOverlayType(Video video, OverlayType overlayType);

    @Query("SELECT vo FROM VideoOverlay vo WHERE vo.video.id = :videoId AND vo.overlayType = :overlayType")
    List<VideoOverlay> findOverlaysByVideoAndType(@Param("videoId") Long videoId,
                                                  @Param("overlayType") OverlayType overlayType);

    // Find overlays for Indian languages (Hindi, Tamil, Telugu, etc.)
    @Query("SELECT vo FROM VideoOverlay vo WHERE vo.language IN ('hi', 'ta', 'te', 'bn', 'mr', 'gu', 'kn', 'ml', 'pa', 'or')")
    List<VideoOverlay> findIndianLanguageOverlays();
}
