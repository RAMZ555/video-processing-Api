package com.videoprocessing.repository;

import com.videoprocessing.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByUuid(String uuid);

    List<Video> findByOrderByUploadTimeDesc();

    @Query("SELECT v FROM Video v WHERE v.duration > :minDuration")
    List<Video> findByDurationGreaterThan(@Param("minDuration") Double minDuration);

    @Query("SELECT v FROM Video v WHERE v.size BETWEEN :minSize AND :maxSize")
    List<Video> findBySizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);
}
