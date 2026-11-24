package com.kopo.portal.repository;

import com.kopo.portal.model.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    long countByStatus(String status);
}
