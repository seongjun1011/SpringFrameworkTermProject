package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 학생(계정) 엔티티.
 * 실제 학사정보시스템처럼 회원가입 없이 학교(학사팀)가 학적 정보를 미리 등록해 두고,
 * 초기 비밀번호는 생년월일 6자리(yyMMdd)로 발급한다. 최초 로그인 시 비밀번호 변경을 강제한다.
 */
@Entity
@Table(name = "students")
public class Student {

    public static final int MAX_LOGIN_FAIL = 5;
    private static final DateTimeFormatter BIRTH_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    private String department;
    private String campus;
    private String grade;
    private LocalDate birthDate;

    /** 초기 비밀번호(또는 초기화된 비밀번호) 사용 중이면 true → 로그인 후 비밀번호 변경 화면으로 보낸다. */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE NOT NULL")
    private boolean passwordChangeRequired;

    /** 연속 로그인 실패 횟수. MAX_LOGIN_FAIL회가 되면 계정을 잠근다. */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0 NOT NULL")
    private int loginFailCount;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE NOT NULL")
    private boolean locked;

    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    // 수정 가능한 개인정보
    private String phone;
    private String address;
    private String accountNumber;

    public Student() {
    }

    public Student(String loginId, String password, String name,
                   String department, String campus, String grade) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.department = department;
        this.campus = campus;
        this.grade = grade;
    }

    /** 학사팀이 학생을 등록할 때 사용: 초기 비밀번호는 생년월일 6자리, 최초 로그인 시 변경 필요. */
    public static Student register(String loginId, String encodedInitialPassword, String name,
                                   String department, String campus, String grade, LocalDate birthDate) {
        Student s = new Student(loginId, encodedInitialPassword, name, department, campus, grade);
        s.birthDate = birthDate;
        s.passwordChangeRequired = true;
        return s;
    }

    /** 생년월일을 초기 비밀번호 형식(yyMMdd)으로 반환한다. */
    public String getBirthDateCode() {
        return birthDate == null ? null : birthDate.format(BIRTH_FORMAT);
    }

    public int getRemainingLoginAttempts() {
        return Math.max(0, MAX_LOGIN_FAIL - loginFailCount);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }

    public int getLoginFailCount() { return loginFailCount; }
    public void setLoginFailCount(int loginFailCount) { this.loginFailCount = loginFailCount; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
