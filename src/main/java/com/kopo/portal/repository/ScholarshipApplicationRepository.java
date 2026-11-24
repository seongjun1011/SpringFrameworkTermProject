package com.kopo.portal.repository;

import com.kopo.portal.model.ScholarshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Long> {

    List<ScholarshipApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    long countByStatus(String status);
}
