package com.kopo.portal.controller;

import com.kopo.portal.model.Staff;
import com.kopo.portal.service.ApprovalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 결재자 > 결재함: 내 차례인 휴학·복학 / 장학금 신청을 승인하거나 반려한다.
 * (AdminInterceptor가 결재자 역할만 접근하도록 검사한다.)
 */
@Controller
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/admin/approvals")
    public String inbox(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("loginStaff");
        model.addAttribute("pending", approvalService.findPendingFor(staff));
        model.addAttribute("processed", approvalService.findProcessedBy(staff));
        return "admin/approvals";
    }

    @PostMapping("/admin/approvals/{kind}/{id}/approve")
    public String approve(@PathVariable String kind, @PathVariable Long id,
                          @RequestParam(defaultValue = "") String comment,
                          HttpSession session, RedirectAttributes ra) {
        return decide(kind, id, comment, true, session, ra);
    }

    @PostMapping("/admin/approvals/{kind}/{id}/reject")
    public String reject(@PathVariable String kind, @PathVariable Long id,
                         @RequestParam(defaultValue = "") String comment,
                         HttpSession session, RedirectAttributes ra) {
        return decide(kind, id, comment, false, session, ra);
    }

    private String decide(String kind, Long id, String comment, boolean approve,
                          HttpSession session, RedirectAttributes ra) {
        Staff staff = (Staff) session.getAttribute("loginStaff");
        String error = approvalService.decide(kind, id, staff, approve, comment);
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("success", approve ? "승인했습니다." : "반려했습니다.");
        }
        return "redirect:/admin/approvals";
    }
}
