package com.videoprocessing.repository;

import com.videoprocessing.entity.*;
import com.videoprocessing.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
    Optional<ProcessingJob> findByJobId(String jobId);

    List<ProcessingJob> findByStatus(JobStatus status);

    List<ProcessingJob> findByJobType(JobType jobType);

    List<ProcessingJob> findByVideoId(Long videoId);

    @Query("SELECT pj FROM ProcessingJob pj WHERE pj.status = :status ORDER BY pj.createdAt ASC")
    List<ProcessingJob> findPendingJobsInOrder(@Param("status") JobStatus status);

    // Count jobs by status
    long countByStatus(JobStatus status);

    // Find failed jobs for retry
    @Query("SELECT pj FROM ProcessingJob pj " +
            "WHERE pj.status = com.videoprocessing.enums.JobStatus.FAILED " +
            "AND pj.createdAt > :cutoff")
    List<ProcessingJob> findRecentFailedJobs(@Param("cutoff") LocalDateTime cutoff);

}
