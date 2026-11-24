package com.kopo.portal.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결재를 거치는 신청서(휴학·복학 신청, 장학금 신청)의 공통 형태.
 * 결재 처리(ApprovalService)가 두 신청을 같은 방식으로 다루기 위해 사용한다.
 */
public interface ApprovalDocument {

    Long getId();
    Student getStudent();
    String getType();
    String getReason();
    String getStatus();
    LocalDateTime getAppliedAt();
    List<ApprovalStep> getApprovalSteps();
    List<UploadedFile> getUploadedFiles();
    void recalcStatus();

    /** 아직 승인되지 않은 첫 번째 결재 단계(= 지금 결재할 차례). 반려됐거나 모두 승인됐으면 -1. */
    default int currentStepIndex() {
        List<ApprovalStep> steps = getApprovalSteps();
        for (int i = 0; i < steps.size(); i++) {
            String st = steps.get(i).getStatus();
            if (ApprovalStep.REJECTED.equals(st)) return -1;
            if (!ApprovalStep.APPROVED.equals(st)) return i;
        }
        return -1;
    }
}
