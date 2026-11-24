package com.kopo.portal.repository;

import com.kopo.portal.model.LeaveApplication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    long countByStatus(String status);

    /** 결재 진행 중인 신청 (오래된 순) */
    List<LeaveApplication> findByStatusOrderByAppliedAtAsc(String status);

    /** 특정 교직원이 처리한 결재단계가 있는 신청 */
    @Query("select distinct a from LeaveApplication a join a.approvalSteps s where s.processedBy.id = :staffId")
    List<LeaveApplication> findProcessedBy(@Param("staffId") Long staffId);
}
