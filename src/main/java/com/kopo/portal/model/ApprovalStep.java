package com.kopo.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/**
 * 결재 단계(담당직원 → 학과장/장학위원회 → 학생처장).
 * 휴학신청·장학금신청에서 공통으로 쓰는 값 타입(@Embeddable)으로,
 * 각 신청 테이블에 딸린 결재단계 테이블(@ElementCollection)에 저장된다.
 */
@Embeddable
public class ApprovalStep {

    @Column(name = "approver_role", length = 30)
    private String role;

    @Column(name = "approver_name", length = 30)
    private String approverName;

    @Column(name = "step_status", length = 10)
    private String status;          // 대기중 / 승인 / 반려

    private LocalDateTime processedAt;

    @Column(name = "step_comment")
    private String comment;

    protected ApprovalStep() {
    }

    public ApprovalStep(String role, String approverName) {
        this.role = role;
        this.approverName = approverName;
        this.status = "대기중";
    }

    public void approve(String comment, LocalDateTime at) {
        this.status = "승인";
        this.comment = comment;
        this.processedAt = at;
    }

    public void reject(String comment, LocalDateTime at) {
        this.status = "반려";
        this.comment = comment;
        this.processedAt = at;
    }

    public String getRole() { return role; }
    public String getApproverName() { return approverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
