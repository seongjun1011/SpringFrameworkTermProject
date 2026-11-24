package com.kopo.portal.repository;

import com.kopo.portal.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByStaffNo(String staffNo);
}
