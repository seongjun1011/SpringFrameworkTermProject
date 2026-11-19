package com.kopo.portal.service;

import com.kopo.portal.model.Student;
import com.kopo.portal.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/** 회원가입 / 로그인 / 개인정보 수정 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByLoginId(String loginId) {
        return studentRepository.existsByLoginId(loginId);
    }

    @Transactional
    public Student signup(String loginId, String rawPassword, String name,
                          String department, String campus, String grade) {
        if (studentRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 학번/아이디입니다.");
        }
        return studentRepository.save(new Student(loginId, passwordEncoder.encode(rawPassword),
                name, department, campus, grade));
    }

    public Optional<Student> login(String loginId, String rawPassword) {
        return studentRepository.findByLoginId(loginId)
                .filter(s -> passwordEncoder.matches(rawPassword, s.getPassword()));
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /** 마지막 로그인 시각/IP를 DB에 저장하고 갱신된 엔티티를 돌려준다. */
    @Transactional
    public Student recordLogin(Student student, String ip) {
        Student s = studentRepository.findById(student.getId()).orElseThrow();
        s.setLastLoginAt(LocalDateTime.now());
        s.setLastLoginIp(ip);
        return s;
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
