package com.kopo.portal.service;

import com.kopo.portal.model.ScholarshipApplication;
import com.kopo.portal.repository.ScholarshipApplicationRepository;
import com.kopo.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** 장학금 신청/조회 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class ScholarshipService {

    @Value("${upload.dir.scholarship}")
    private String uploadDir;

    private final ScholarshipApplicationRepository scholarshipRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public ScholarshipService(ScholarshipApplicationRepository scholarshipRepository,
            StudentRepository studentRepository,
            FileStorageService fileStorageService) {
        this.scholarshipRepository = scholarshipRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ScholarshipApplication apply(Long studentId, String type, String reason, MultipartFile[] files) throws IOException {
        ScholarshipApplication app = new ScholarshipApplication(
                studentRepository.getReferenceById(studentId), type, reason, LocalDateTime.now());
        app.getUploadedFiles().addAll(fileStorageService.store(uploadDir, files));
        return scholarshipRepository.save(app);
    }

    public List<ScholarshipApplication> findByStudent(Long studentId) {
        return scholarshipRepository.findByStudentIdOrderByAppliedAtDesc(studentId);
    }
}
