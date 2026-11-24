package com.kopo.portal.service;

import com.kopo.portal.model.AcademicSchedule;
import com.kopo.portal.repository.AcademicScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /** 관리자 화면용: 최근 일정부터 */
    public List<AcademicSchedule> findAllLatestFirst() {
        return scheduleRepository.findAllByOrderByStartDateDesc();
    }

    /**
     * 학사일정 등록. 하루짜리 일정은 종료일을 비워 두면 시작일과 같게 저장한다.
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String create(String title, LocalDate startDate, LocalDate endDate) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty() || t.length() > 100) return "일정명을 1~100자로 입력하세요.";
        if (startDate == null) return "시작일을 입력하세요.";
        LocalDate end = endDate == null ? startDate : endDate;
        if (end.isBefore(startDate)) return "종료일은 시작일과 같거나 이후여야 합니다.";
        scheduleRepository.save(new AcademicSchedule(startDate, end, t));
        return null;
    }
}
