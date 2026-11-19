package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * 학사일정 한 건을 표현하는 엔티티.
 */
@Entity
@Table(name = "academic_schedules")
public class AcademicSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;  // 단일 날짜 일정이면 startDate와 동일

    @Column(nullable = false, length = 100)
    private String title;

    protected AcademicSchedule() {
    }

    public AcademicSchedule(LocalDate startDate, LocalDate endDate, String title) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    /** 화면에 "06-16~06-22" 또는 "06-30" 형태로 표시하기 위한 헬퍼 */
    public String getDisplayRange() {
        String start = String.format("%02d-%02d", startDate.getMonthValue(), startDate.getDayOfMonth());
        if (endDate == null || endDate.equals(startDate)) {
            return start;
        }
        String end = String.format("%02d-%02d", endDate.getMonthValue(), endDate.getDayOfMonth());
        return start + "~" + end;
    }
}
