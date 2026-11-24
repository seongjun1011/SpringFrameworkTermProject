package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 결재 단계(담당직원 → 학과장/장학위원회 → 학생처장).
 * 휴학신청·장학금신청에서 공통으로 쓰는 값 타입(@Embeddable)으로,
 * 각 신청 테이블에 딸린 결재단계 테이블(@ElementCollection)에 저장된다.
 */
@Embeddable
public class ApprovalStep {

    public static final String WAITING  = "대기중";
    public static final String APPROVED = "승인";
    public static final String REJECTED = "반려";

    @Column(name = "approver_role", length = 30)
    private String role;

    @Column(name = "approver_name", length = 30)
    private String approverName;

    @Column(name = "step_status", length = 10)
    private String status;          // 대기중 / 승인 / 반려

    private LocalDateTime processedAt;

    @Column(name = "step_comment")
    private String comment;

    /** 이 단계를 처리한 교직원 (대기 중이면 null) → staffs.id 외래키 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Staff processedBy;

    protected ApprovalStep() {
    }

    public ApprovalStep(String role, String approverName) {
        this.role = role;
        this.approverName = approverName;
        this.status = WAITING;
    }

    public void approve(String comment, LocalDateTime at) {
        this.status = APPROVED;
        this.comment = comment;
        this.processedAt = at;
    }

    public void reject(String comment, LocalDateTime at) {
        this.status = REJECTED;
        this.comment = comment;
        this.processedAt = at;
    }

    /** 교직원이 결재함에서 승인/반려할 때: 처리자와 실제 결재자 이름을 함께 기록한다. */
    public void decide(boolean approve, Staff staff, String comment, LocalDateTime at) {
        if (approve) approve(comment, at);
        else reject(comment, at);
        this.processedBy = staff;
        this.approverName = staff.getName();
    }

    public String getRole() { return role; }
    public Staff getProcessedBy() { return processedBy; }
    public String getApproverName() { return approverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
