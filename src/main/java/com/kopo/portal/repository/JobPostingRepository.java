package com.kopo.portal.repository;

import com.kopo.portal.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findAllByOrderByRequestedDateDesc();
}
