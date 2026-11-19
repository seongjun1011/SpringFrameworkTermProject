package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 휴학/복학 신청 엔티티.
 * 결재 단계와 첨부파일은 @ElementCollection 으로 별도 테이블에 저장된다.
 */
@Entity
@Table(name = "leave_applications")
public class LeaveApplication {

    public static final String STATUS_PENDING  = "처리중";
    public static final String STATUS_APPROVED = "승인";
    public static final String STATUS_REJECTED = "반려";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "apply_type", nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "leave_applications_steps", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "step_order")
    private List<ApprovalStep> approvalSteps = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "leave_applications_files", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "file_order")
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    protected LeaveApplication() {
    }

    public LeaveApplication(Student student, String type, String reason, LocalDateTime appliedAt) {
        this.student = student;
        this.type = type;
        this.reason = reason;
        this.appliedAt = appliedAt;
        this.approvalSteps.add(new ApprovalStep("담당직원", "이수진"));
        this.approvalSteps.add(new ApprovalStep("학과장", "최병철"));
        this.approvalSteps.add(new ApprovalStep("학생처장", "김태호"));
        this.status = STATUS_PENDING;
    }

    /** 결재 단계 상태로부터 전체 상태를 다시 계산한다. */
    public void recalcStatus() {
        boolean anyRejected = approvalSteps.stream().anyMatch(s -> "반려".equals(s.getStatus()));
        boolean allApproved = approvalSteps.stream().allMatch(s -> "승인".equals(s.getStatus()));
        if (anyRejected) this.status = STATUS_REJECTED;
        else if (allApproved) this.status = STATUS_APPROVED;
        else this.status = STATUS_PENDING;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getType() { return type; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public List<ApprovalStep> getApprovalSteps() { return approvalSteps; }
    public List<UploadedFile> getUploadedFiles() { return uploadedFiles; }
}
