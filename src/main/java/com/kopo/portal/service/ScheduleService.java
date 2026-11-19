package com.kopo.portal.service;

import com.kopo.portal.model.AcademicSchedule;
import com.kopo.portal.repository.AcademicScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

/** 학사일정 조회 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class ScheduleService {

    private final AcademicScheduleRepository scheduleRepository;

    public ScheduleService(AcademicScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /** 해당 월과 기간이 겹치는 일정을 시작일 순으로 반환한다. */
    public List<AcademicSchedule> findByMonth(YearMonth month) {
        return scheduleRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateAsc(
                month.atEndOfMonth(), month.atDay(1));
    }

    public List<AcademicSchedule> findAll() {
        return scheduleRepository.findAll();
    }
}
