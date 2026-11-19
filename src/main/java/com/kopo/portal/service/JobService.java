package com.kopo.portal.service;

import com.kopo.portal.model.JobPosting;
import com.kopo.portal.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** 채용정보 조회 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobPostingRepository jobPostingRepository;

    public JobService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    public List<JobPosting> findAll() {
        return jobPostingRepository.findAllByOrderByRequestedDateDesc();
    }

    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }
}
