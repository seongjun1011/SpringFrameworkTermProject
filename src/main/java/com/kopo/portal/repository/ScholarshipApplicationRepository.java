package com.kopo.portal.repository;

import com.kopo.portal.model.ScholarshipApplication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Long> {

    List<ScholarshipApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    long countByStatus(String status);

    /** 결재 진행 중인 신청 (오래된 순) */
    List<ScholarshipApplication> findByStatusOrderByAppliedAtAsc(String status);

    /** 특정 교직원이 처리한 결재단계가 있는 신청 */
    @Query("select distinct a from ScholarshipApplication a join a.approvalSteps s where s.processedBy.id = :staffId")
    List<ScholarshipApplication> findProcessedBy(@Param("staffId") Long staffId);
}
