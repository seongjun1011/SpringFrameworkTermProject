package com.kopo.portal.controller;

import com.kopo.portal.model.Student;
import com.kopo.portal.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * 로그인/로그아웃을 처리하는 컨트롤러.
 * 로그인/회원가입 처리 (DB 연동). 데모 계정: 2301110329 / 1234
 */
@Controller
public class LoginController {

    private final StudentService studentService;

    public LoginController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String signup, Model model) {
        if (signup != null) {
            model.addAttribute("success", "회원가입이 완료되었습니다. 로그인해 주세요.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                         @RequestParam String password,
                         HttpServletRequest request,
                         Model model) {
        Optional<Student> result = studentService.login(loginId, password);
        if (result.isEmpty()) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "login";
        }
        Student student = studentService.recordLogin(result.get(), request.getRemoteAddr());
        request.getSession().setAttribute("loginStudent", student);
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String loginId,
                         @RequestParam String password,
                         @RequestParam String passwordConfirm,
                         @RequestParam String name,
                         @RequestParam(defaultValue = "인공지능소프트웨어과") String department,
                         @RequestParam(defaultValue = "서울정수캠퍼스") String campus,
                         @RequestParam(defaultValue = "1학년") String grade,
                         Model model) {
        loginId = loginId.trim();
        name = name.trim();
        String error = null;
        if (loginId.length() < 4 || loginId.length() > 20) {
            error = "학번/아이디는 4~20자로 입력하세요.";
        } else if (name.isEmpty()) {
            error = "이름을 입력하세요.";
        } else if (password.length() < 4) {
            error = "비밀번호는 4자 이상 입력하세요.";
        } else if (!password.equals(passwordConfirm)) {
            error = "비밀번호 확인이 일치하지 않습니다.";
        } else if (studentService.existsByLoginId(loginId)) {
            error = "이미 사용 중인 학번/아이디입니다.";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            return "signup";
        }
        studentService.signup(loginId, password, name, department, campus, grade);
        return "redirect:/login?signup=success";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
