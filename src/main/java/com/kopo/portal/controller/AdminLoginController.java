package com.kopo.portal.controller;

import com.kopo.portal.model.LeaveApplication;
import com.kopo.portal.model.ScholarshipApplication;
import com.kopo.portal.model.Staff;
import com.kopo.portal.repository.LeaveApplicationRepository;
import com.kopo.portal.repository.ScholarshipApplicationRepository;
import com.kopo.portal.repository.StudentRepository;
import com.kopo.portal.service.ApprovalService;
import com.kopo.portal.service.StaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * 교직원(관리자) 로그인 / 로그아웃 / 관리자 홈.
 * 교직원 세션(loginStaff)은 학생 세션(loginStudent)과 별도 속성으로 저장한다.
 */
@Controller
public class AdminLoginController {

    private final StaffService staffService;
    private final ApprovalService approvalService;
    private final StudentRepository studentRepository;
    private final LeaveApplicationRepository leaveRepository;
    private final ScholarshipApplicationRepository scholarshipRepository;

    public AdminLoginController(StaffService staffService, ApprovalService approvalService,
                                StudentRepository studentRepository,
                                LeaveApplicationRepository leaveRepository,
                                ScholarshipApplicationRepository scholarshipRepository) {
        this.staffService = staffService;
        this.approvalService = approvalService;
        this.studentRepository = studentRepository;
        this.leaveRepository = leaveRepository;
        this.scholarshipRepository = scholarshipRepository;
    }

    @GetMapping("/admin/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute("loginStaff") != null) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String staffNo,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        Optional<Staff> staff = staffService.login(staffNo, password);
        if (staff.isEmpty()) {
            model.addAttribute("error", "사번 또는 비밀번호가 올바르지 않습니다.");
            model.addAttribute("staffNo", staffNo);
            return "admin/login";
        }
        // 세션 고정 공격 방지: 세션 ID를 새로 발급한다(기존 속성은 유지).
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute("loginStaff", staff.get());
        return "redirect:/admin";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginStaff");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String home(@RequestParam(required = false) String denied, HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("loginStaff");
        model.addAttribute("staff", staff);
        if (denied != null) {
            model.addAttribute("error", staff.getRole().getLabel() + " 권한으로는 사용할 수 없는 메뉴입니다.");
        }
        if (staff.getRole().isAcademicAdmin()) {
            model.addAttribute("studentCount", studentRepository.count());
            model.addAttribute("lockedCount", studentRepository.countByLockedTrue());
        }
        if (staff.getRole().isApprover()) {
            model.addAttribute("myPendingCount", approvalService.findPendingFor(staff).size());
        }
        model.addAttribute("pendingLeave", leaveRepository.countByStatus(LeaveApplication.STATUS_PENDING));
        model.addAttribute("pendingScholarship",
                scholarshipRepository.countByStatus(ScholarshipApplication.STATUS_PENDING));
        return "admin/home";
    }
}
