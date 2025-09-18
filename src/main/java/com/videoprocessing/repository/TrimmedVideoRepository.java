package com.videoprocessing.repository;

import com.videoprocessing.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrimmedVideoRepository extends JpaRepository<TrimmedVideo, Long> {
    Optional<TrimmedVideo> findByUuid(String uuid);

    List<TrimmedVideo> findByOriginalVideoId(Long originalVideoId);

    List<TrimmedVideo> findByOriginalVideoOrderByCreatedAtDesc(Video originalVideo);
}
