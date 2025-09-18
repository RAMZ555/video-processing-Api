
package com.videoprocessing.repository;

import com.videoprocessing.entity.*;
import com.videoprocessing.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoQualityVersionRepositor extends JpaRepository<VideoQualityVersion, Long> {
    List<VideoQualityVersion> findByOriginalVideoId(Long originalVideoId);

    Optional<VideoQualityVersion> findByOriginalVideoAndQuality(Video originalVideo, VideoQuality quality);

    @Query("SELECT vqv FROM VideoQualityVersion vqv WHERE vqv.originalVideo.id = :videoId AND vqv.quality = :quality")
    Optional<VideoQualityVersion> findVideoByQuality(@Param("videoId") Long videoId,
                                                     @Param("quality") VideoQuality quality);

    // Check if all qualities exist for a video
    @Query("SELECT COUNT(vqv) FROM VideoQualityVersion vqv WHERE vqv.originalVideo.id = :videoId")
    long countQualityVersionsByVideo(@Param("videoId") Long videoId);

    // Get total storage used by quality versions
    @Query("SELECT SUM(vqv.size) FROM VideoQualityVersion vqv WHERE vqv.originalVideo.id = :videoId")
    Long getTotalSizeByVideo(@Param("videoId") Long videoId);
}