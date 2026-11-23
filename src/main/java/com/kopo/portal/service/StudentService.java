package com.kopo.portal.service;

import com.kopo.portal.model.Student;
import com.kopo.portal.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * 로그인 / 비밀번호 변경·초기화 / 개인정보 수정 (JPA 사용).
 * 회원가입은 없다. 학생 계정은 학사팀이 미리 등록하고(DataInitializer),
 * 초기 비밀번호는 생년월일 6자리(yyMMdd)다.
 */
@Service
@Transactional(readOnly = true)
public class StudentService {

    /** 로그인 결과. 성공이면 student가 채워지고, 실패면 message에 사유가 담긴다. */
    public record LoginResult(Student student, String message) {
        static LoginResult ok(Student s) { return new LoginResult(s, null); }
        static LoginResult fail(String message) { return new LoginResult(null, message); }
        public boolean success() { return student != null; }
    }

    private static final String BAD_CREDENTIALS = "학번 또는 비밀번호가 올바르지 않습니다.";
    private static final String LOCKED = "비밀번호를 " + Student.MAX_LOGIN_FAIL
            + "회 잘못 입력하여 계정이 잠겼습니다. [비밀번호 초기화]에서 본인 확인 후 다시 이용하세요.";

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 로그인. 비밀번호가 틀리면 실패 횟수를 올리고, 5회가 되면 계정을 잠근다.
     * 성공하면 실패 횟수를 0으로 되돌리고 마지막 로그인 시각/IP를 기록한다.
     */
    @Transactional
    public LoginResult login(String loginId, String rawPassword, String ip) {
        Optional<Student> found = studentRepository.findByLoginId(loginId.trim());
        if (found.isEmpty()) {
            return LoginResult.fail(BAD_CREDENTIALS);
        }
        Student s = found.get();
        if (s.isLocked()) {
            return LoginResult.fail(LOCKED);
        }
        if (!passwordEncoder.matches(rawPassword, s.getPassword())) {
            s.setLoginFailCount(s.getLoginFailCount() + 1);
            if (s.getLoginFailCount() >= Student.MAX_LOGIN_FAIL) {
                s.setLocked(true);
                return LoginResult.fail(LOCKED);
            }
            return LoginResult.fail(BAD_CREDENTIALS + " (남은 시도 " + s.getRemainingLoginAttempts() + "회)");
        }
        s.setLoginFailCount(0);
        s.setLastLoginAt(LocalDateTime.now());
        s.setLastLoginIp(ip);
        return LoginResult.ok(s);
    }

    /**
     * 비밀번호 변경 (최초 로그인 시 강제 변경, 또는 로그인 후 자발적 변경).
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String changePassword(Long studentId, String currentPassword,
                                 String newPassword, String newPasswordConfirm) {
        Student s = studentRepository.findById(studentId).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, s.getPassword())) {
            return "현재 비밀번호가 올바르지 않습니다.";
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            return "새 비밀번호 확인이 일치하지 않습니다.";
        }
        String policyError = checkPasswordPolicy(s, newPassword);
        if (policyError != null) {
            return policyError;
        }
        if (passwordEncoder.matches(newPassword, s.getPassword())) {
            return "현재 비밀번호와 다른 비밀번호를 입력하세요.";
        }
        s.setPassword(passwordEncoder.encode(newPassword));
        s.setPasswordChangeRequired(false);
        return null;
    }

    /** 비밀번호 규칙: 8~20자, 영문과 숫자를 모두 포함, 학번·생년월일 사용 금지. */
    private String checkPasswordPolicy(Student s, String pw) {
        if (pw.length() < 8 || pw.length() > 20) {
            return "비밀번호는 8~20자로 입력하세요.";
        }
        if (!pw.matches(".*[A-Za-z].*") || !pw.matches(".*[0-9].*")) {
            return "비밀번호는 영문과 숫자를 모두 포함해야 합니다.";
        }
        if (pw.contains(s.getLoginId())
                || (s.getBirthDateCode() != null && pw.contains(s.getBirthDateCode()))) {
            return "학번이나 생년월일이 포함된 비밀번호는 사용할 수 없습니다.";
        }
        return null;
    }

    /**
     * 비밀번호 초기화. 학번·이름·생년월일이 학적 정보와 일치하면
     * 비밀번호를 생년월일 6자리로 되돌리고, 잠금을 풀고, 다음 로그인 시 변경하도록 표시한다.
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String resetPassword(String loginId, String name, String birthCode) {
        String code = birthCode.trim();
        try {
            DateTimeFormatter.ofPattern("yyMMdd").parse(code);
        } catch (DateTimeParseException e) {
            return "생년월일은 6자리 숫자(예: 030101)로 입력하세요.";
        }
        Optional<Student> found = studentRepository.findByLoginId(loginId.trim())
                .filter(s -> s.getName().equals(name.trim()))
                .filter(s -> code.equals(s.getBirthDateCode()));
        if (found.isEmpty()) {
            return "입력한 정보와 일치하는 학적 정보가 없습니다. 학사지원팀에 문의하세요.";
        }
        Student s = found.get();
        s.setPassword(passwordEncoder.encode(s.getBirthDateCode()));
        s.setPasswordChangeRequired(true);
        s.setLoginFailCount(0);
        s.setLocked(false);
        return null;
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional
    public Student updateProfile(Long id, String phone, String address, String accountNumber) {
        Student s = studentRepository.findById(id).orElseThrow();
        s.setPhone(phone);
        s.setAddress(address);
        s.setAccountNumber(accountNumber);
        return s;
    }
}
