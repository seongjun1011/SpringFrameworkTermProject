package com.kopo.portal.controller;

import com.kopo.portal.model.Student;
import com.kopo.portal.service.StudentService;
import com.kopo.portal.service.StudentService.LoginResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 로그인 / 로그아웃 / 비밀번호 변경 / 비밀번호 초기화 컨트롤러.
 * 학사정보시스템이므로 회원가입은 없다. 계정은 학사팀이 미리 등록하고
 * 초기 비밀번호(생년월일 6자리)로 최초 로그인한 뒤 비밀번호를 변경한다.
 */
@Controller
public class LoginController {

    private final StudentService studentService;

    public LoginController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String reset, Model model) {
        if (reset != null) {
            model.addAttribute("success", "비밀번호가 생년월일 6자리로 초기화되었습니다. 로그인 후 새 비밀번호로 변경하세요.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        LoginResult result = studentService.login(loginId, password, request.getRemoteAddr());
        if (!result.success()) {
            model.addAttribute("error", result.message());
            model.addAttribute("loginId", loginId);
            return "login";
        }
        // 세션 고정 공격 방지: 로그인 성공 시 기존 세션을 버리고 새 세션을 만든다.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        Student student = result.student();
        request.getSession(true).setAttribute("loginStudent", student);
        if (student.isPasswordChangeRequired()) {
            return "redirect:/password/change";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== 비밀번호 변경 (로그인 필요) =====

    @GetMapping("/password/change")
    public String changeForm(HttpSession session, Model model) {
        model.addAttribute("student", session.getAttribute("loginStudent"));
        return "password-change";
    }

    @PostMapping("/password/change")
    public String change(@RequestParam String currentPassword,
                         @RequestParam String newPassword,
                         @RequestParam String newPasswordConfirm,
                         HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loginStudent");
        String error = studentService.changePassword(
                student.getId(), currentPassword, newPassword, newPasswordConfirm);
        if (error != null) {
            model.addAttribute("student", student);
            model.addAttribute("error", error);
            return "password-change";
        }
        boolean wasFirstLogin = student.isPasswordChangeRequired();
        Student updated = studentService.findById(student.getId()).orElseThrow();
        session.setAttribute("loginStudent", updated);
        if (wasFirstLogin) {
            return "redirect:/";
        }
        model.addAttribute("student", updated);
        model.addAttribute("success", "비밀번호가 변경되었습니다.");
        return "password-change";
    }

    // ===== 비밀번호 초기화 (로그인 불필요) =====

    @GetMapping("/password/reset")
    public String resetForm() {
        return "password-reset";
    }

    @PostMapping("/password/reset")
    public String reset(@RequestParam String loginId,
                        @RequestParam String name,
                        @RequestParam String birthCode,
                        Model model) {
        String error = studentService.resetPassword(loginId, name, birthCode);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            return "password-reset";
        }
        return "redirect:/login?reset";
    }
}
