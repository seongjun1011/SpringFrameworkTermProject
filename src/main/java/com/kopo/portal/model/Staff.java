package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 교직원(관리자) 계정 엔티티.
 * 학생(students)과 가진 정보가 달라(사번·소속·역할) 별도 테이블로 관리한다.
 */
@Entity
@Table(name = "staffs")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사번 (로그인 아이디) */
    @Column(nullable = false, unique = true, length = 20)
    private String staffNo;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 50)
    private String department;

    /** 역할을 이름(문자열)으로 저장해, enum 순서가 바뀌어도 데이터가 깨지지 않게 한다. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StaffRole role;

    private LocalDateTime lastLoginAt;

    protected Staff() {
    }

    public Staff(String staffNo, String encodedPassword, String name, String department, StaffRole role) {
        this.staffNo = staffNo;
        this.password = encodedPassword;
        this.name = name;
        this.department = department;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getStaffNo() { return staffNo; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public StaffRole getRole() { return role; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
