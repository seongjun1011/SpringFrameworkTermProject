package com.kopo.portal.repository;

import com.kopo.portal.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    long countByLockedTrue();

    /** 관리자 학생 목록: 학번 또는 이름에 검색어가 포함된 학생 (페이징) */
    Page<Student> findByLoginIdContainingOrNameContaining(String loginId, String name, Pageable pageable);
}
