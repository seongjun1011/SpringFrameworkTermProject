package com.kopo.portal.repository;

import com.kopo.portal.model.AcademicSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AcademicScheduleRepository extends JpaRepository<AcademicSchedule, Long> {

    /** 기간이 [from, to] 구간과 겹치는 일정 (startDate <= to AND endDate >= from) */
    List<AcademicSchedule> findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
            LocalDate to, LocalDate from);

    List<AcademicSchedule> findAllByOrderByStartDateDesc();
}
