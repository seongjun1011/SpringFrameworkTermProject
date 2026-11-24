package com.kopo.portal.service;

import com.kopo.portal.model.Notice;
import com.kopo.portal.model.Staff;
import com.kopo.portal.repository.NoticeRepository;
import com.kopo.portal.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 공지사항 조회, 학사관리자의 공지 작성 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final StaffRepository staffRepository;

    public NoticeService(NoticeRepository noticeRepository, StaffRepository staffRepository) {
        this.noticeRepository = noticeRepository;
        this.staffRepository = staffRepository;
    }

    public List<Notice> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Notice> findById(Long id) {
        return noticeRepository.findById(id);
    }

    /**
     * 공지 작성. 작성자는 notices.staff_id(→ staffs.id)로 연결하고, 화면 표시용 작성 부서도 함께 저장한다.
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String write(Staff staff, String title, String content) {
        String t = title == null ? "" : title.trim();
        String c = content == null ? "" : content.trim();
        if (t.isEmpty() || t.length() > 100) return "제목을 1~100자로 입력하세요.";
        if (c.isEmpty() || c.length() > 2000) return "내용을 1~2000자로 입력하세요.";
        noticeRepository.save(new Notice(t, c, staffRepository.getReferenceById(staff.getId()), LocalDateTime.now()));
        return null;
    }
}
