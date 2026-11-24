package com.kopo.portal.service;

import com.kopo.portal.model.ApprovalDocument;
import com.kopo.portal.model.ApprovalStep;
import com.kopo.portal.model.LeaveApplication;
import com.kopo.portal.model.ScholarshipApplication;
import com.kopo.portal.model.Staff;
import com.kopo.portal.model.UploadedFile;
import com.kopo.portal.repository.LeaveApplicationRepository;
import com.kopo.portal.repository.ScholarshipApplicationRepository;
import com.kopo.portal.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 휴학·복학 / 장학금 신청의 결재 처리 (결재자용).
 * 결재는 단계 순서대로 진행되며, 앞 단계가 모두 승인되어 "내 역할의 차례"가 된 신청만 처리할 수 있다.
 */
@Service
@Transactional(readOnly = true)
public class ApprovalService {

    public static final String LEAVE = "leave";
    public static final String SCHOLARSHIP = "scholarship";

    /** 화면 표시용 결재 단계 (지연 로딩된 엔티티를 트랜잭션 밖으로 넘기지 않기 위해 값만 복사) */
    public record StepView(String role, String approverName, String status, LocalDateTime processedAt, String comment) {
    }

    /** 결재함 화면에 보여줄 신청 1건 */
    public record ApprovalItem(String kind, String kindLabel, Long id, String studentNo, String studentName,
                               String department, String applyType, String reason, String status,
                               LocalDateTime appliedAt, List<StepView> steps, List<String> fileNames,
                               int currentStep, LocalDateTime myProcessedAt) {
    }

    private final LeaveApplicationRepository leaveRepository;
    private final ScholarshipApplicationRepository scholarshipRepository;
    private final StaffRepository staffRepository;

    public ApprovalService(LeaveApplicationRepository leaveRepository,
                           ScholarshipApplicationRepository scholarshipRepository,
                           StaffRepository staffRepository) {
        this.leaveRepository = leaveRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.staffRepository = staffRepository;
    }

    /** 지금 이 교직원이 결재할 차례인 신청 목록 (오래된 순) */
    public List<ApprovalItem> findPendingFor(Staff staff) {
        List<ApprovalItem> result = new ArrayList<>();
        for (LeaveApplication a : leaveRepository.findByStatusOrderByAppliedAtAsc(LeaveApplication.STATUS_PENDING)) {
            if (isMyTurn(a, staff)) result.add(toItem(LEAVE, a, staff));
        }
        for (ScholarshipApplication a :
                scholarshipRepository.findByStatusOrderByAppliedAtAsc(ScholarshipApplication.STATUS_PENDING)) {
            if (isMyTurn(a, staff)) result.add(toItem(SCHOLARSHIP, a, staff));
        }
        result.sort(Comparator.comparing(ApprovalItem::appliedAt));
        return result;
    }

    /** 이 교직원이 처리한 신청 목록 (최근 처리 순) */
    public List<ApprovalItem> findProcessedBy(Staff staff) {
        return Stream.concat(
                        leaveRepository.findProcessedBy(staff.getId()).stream().map(a -> toItem(LEAVE, a, staff)),
                        scholarshipRepository.findProcessedBy(staff.getId()).stream().map(a -> toItem(SCHOLARSHIP, a, staff)))
                .sorted(Comparator.comparing(ApprovalItem::myProcessedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /**
     * 승인 또는 반려. 신청서 전체 상태(처리중/승인/반려)도 다시 계산한다.
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String decide(String kind, Long id, Staff staff, boolean approve, String comment) {
        ApprovalDocument doc = switch (kind) {
            case LEAVE -> leaveRepository.findById(id).orElse(null);
            case SCHOLARSHIP -> scholarshipRepository.findById(id).orElse(null);
            default -> null;
        };
        if (doc == null) {
            return "존재하지 않는 신청입니다.";
        }
        if (!isMyTurn(doc, staff)) {
            return "지금은 " + staff.getRole().getLabel() + " 결재 차례가 아니거나 이미 처리된 신청입니다.";
        }
        String text = comment == null ? "" : comment.trim();
        if (!approve && text.isEmpty()) {
            return "반려할 때는 반려 사유(의견)를 입력하세요.";
        }
        if (text.length() > 200) {
            return "의견은 200자 이내로 입력하세요.";
        }
        ApprovalStep step = doc.getApprovalSteps().get(doc.currentStepIndex());
        step.decide(approve, staffRepository.getReferenceById(staff.getId()),
                text.isEmpty() ? (approve ? "승인" : null) : text, LocalDateTime.now());
        doc.recalcStatus();   // 변경 감지로 결재단계·신청서 UPDATE
        return null;
    }

    /** 처리할 결재 단계가 남아 있고, 그 단계의 결재자 역할이 로그인한 교직원의 역할과 같은지 */
    private boolean isMyTurn(ApprovalDocument doc, Staff staff) {
        int idx = doc.currentStepIndex();
        if (idx < 0) return false;
        ApprovalStep step = doc.getApprovalSteps().get(idx);
        return ApprovalStep.WAITING.equals(step.getStatus()) && staff.getRole().getLabel().equals(step.getRole());
    }

    private ApprovalItem toItem(String kind, ApprovalDocument a, Staff staff) {
        List<StepView> steps = a.getApprovalSteps().stream()
                .map(s -> new StepView(s.getRole(), s.getApproverName(), s.getStatus(), s.getProcessedAt(), s.getComment()))
                .toList();
        LocalDateTime myProcessedAt = a.getApprovalSteps().stream()
                .filter(s -> s.getProcessedBy() != null && s.getProcessedBy().getId().equals(staff.getId()))
                .map(ApprovalStep::getProcessedAt)
                .findFirst().orElse(null);
        return new ApprovalItem(kind, LEAVE.equals(kind) ? "휴학·복학" : "장학금", a.getId(),
                a.getStudent().getLoginId(), a.getStudent().getName(), a.getStudent().getDepartment(),
                a.getType(), a.getReason(), a.getStatus(), a.getAppliedAt(), steps,
                a.getUploadedFiles().stream().map(UploadedFile::getOriginalName).toList(),
                a.currentStepIndex(), myProcessedAt);
    }
}
