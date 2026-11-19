package com.kopo.portal.repository;

import com.kopo.portal.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedAtDesc();
}
