package com.kopo.portal.service;

import com.kopo.portal.model.Staff;
import com.kopo.portal.repository.StaffRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/** 교직원 로그인 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 사번·비밀번호가 맞으면 마지막 로그인 시각을 기록하고 교직원을 돌려준다. */
    @Transactional
    public Optional<Staff> login(String staffNo, String rawPassword) {
        return staffRepository.findByStaffNo(staffNo.trim())
                .filter(s -> passwordEncoder.matches(rawPassword, s.getPassword()))
                .map(s -> {
                    s.setLastLoginAt(LocalDateTime.now());
                    return s;
                });
    }
}
