package com.kopo.portal.model;

import jakarta.persistence.*;

/**
 * 개설 강좌 엔티티.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String professor;

    @Column(length = 5)
    private String dayOfWeek;

    private int startPeriod;
    private int endPeriod;

    @Column(length = 20)
    private String location;

    private int capacity;

    /** 현재 신청 인원. 수강신청/취소 시 같은 트랜잭션 안에서 함께 증감한다. */
    private int enrolledCount;

    protected Course() {
    }

    public Course(String name, String professor, String dayOfWeek,
                  int startPeriod, int endPeriod, String location, int capacity) {
        this.name = name;
        this.professor = professor;
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.location = location;
        this.capacity = capacity;
        this.enrolledCount = 0;
    }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    public void increaseEnrolled() {
        enrolledCount++;
    }

    public void decreaseEnrolled() {
        enrolledCount = Math.max(0, enrolledCount - 1);
    }

    /** 기존 템플릿 호환용 — "N교시" 또는 "N-M교시" 형태로 반환 */
    public String getPeriod() {
        if (startPeriod == endPeriod) return startPeriod + "교시";
        return startPeriod + "-" + endPeriod + "교시";
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getProfessor() { return professor; }
    public String getDayOfWeek() { return dayOfWeek; }
    public int getStartPeriod() { return startPeriod; }
    public int getEndPeriod() { return endPeriod; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
}
