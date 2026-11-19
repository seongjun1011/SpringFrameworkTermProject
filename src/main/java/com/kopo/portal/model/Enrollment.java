package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 수강신청 내역 엔티티. 학생(Student)과 강좌(Course)의 N:M 관계를 풀어주는 연결 테이블이다.
 * 같은 학생이 같은 강좌를 두 번 신청할 수 없도록 (student_id, course_id)에 유니크 제약을 건다.
 */
@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_student_course",
                columnNames = {"student_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    protected Enrollment() {
    }

    public Enrollment(Student student, Course course, LocalDateTime appliedAt) {
        this.student = student;
        this.course = course;
        this.appliedAt = appliedAt;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
}
