package com.kopo.portal.service;

import com.kopo.portal.model.Notice;
import com.kopo.portal.repository.NoticeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** 공지사항 조회 (JPA 사용). */
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<Notice> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Notice> findById(Long id) {
        return noticeRepository.findById(id);
    }
}
