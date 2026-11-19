package com.kopo.portal.service;

import com.kopo.portal.model.LeaveApplication;
import com.kopo.portal.repository.LeaveApplicationRepository;
import com.kopo.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** 휴학/복학 신청/조회 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class LeaveService {

    @Value("${upload.dir.leave}")
    private String uploadDir;

    private final LeaveApplicationRepository leaveRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public LeaveService(LeaveApplicationRepository leaveRepository,
            StudentRepository studentRepository,
            FileStorageService fileStorageService) {
        this.leaveRepository = leaveRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public LeaveApplication apply(Long studentId, String type, String reason, MultipartFile[] files) throws IOException {
        LeaveApplication app = new LeaveApplication(
                studentRepository.getReferenceById(studentId), type, reason, LocalDateTime.now());
        app.getUploadedFiles().addAll(fileStorageService.store(uploadDir, files));
        return leaveRepository.save(app);
    }

    public List<LeaveApplication> findByStudent(Long studentId) {
        return leaveRepository.findByStudentIdOrderByAppliedAtDesc(studentId);
    }
}
