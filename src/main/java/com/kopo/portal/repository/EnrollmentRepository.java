package com.kopo.portal.repository;

import com.kopo.portal.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentIdOrderByAppliedAtAsc(Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /** 강좌별 수강신청 현황 (신청 순) */
    List<Enrollment> findByCourseIdOrderByAppliedAtAsc(Long courseId);
}
