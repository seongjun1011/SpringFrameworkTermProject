package com.kopo.portal.controller;

import com.kopo.portal.model.Student;
import com.kopo.portal.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * 학사관리자 > 학적 관리: 학생 학적 등록, 학생 목록 조회, 계정 잠금 해제·비밀번호 초기화.
 * (AdminInterceptor가 학사관리자만 접근하도록 검사한다.)
 */
@Controller
public class AdminStudentController {

    private static final int PAGE_SIZE = 10;
    static final List<String> DEPARTMENTS = List.of(
            "인공지능소프트웨어과", "컴퓨터공학과", "정보통신과", "전기과", "기계과");
    static final List<String> CAMPUSES = List.of("서울정수캠퍼스", "서울강서캠퍼스", "인천캠퍼스");
    static final List<String> GRADES = List.of("1학년", "2학년", "3학년", "4학년");

    private final StudentService studentService;

    public AdminStudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/admin/students")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Student> result = studentService.search(q,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("loginId")));
        model.addAttribute("students", result);
        model.addAttribute("q", q);
        return "admin/students";
    }

    @GetMapping("/admin/students/new")
    public String form(Model model) {
        addOptions(model);
        return "admin/student-form";
    }

    @PostMapping("/admin/students/new")
    public String create(@RequestParam String loginId,
                         @RequestParam String name,
                         @RequestParam String department,
                         @RequestParam String campus,
                         @RequestParam String grade,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                         Model model, RedirectAttributes ra) {
        String error = studentService.register(loginId, name, department, campus, grade, birthDate);
        if (error != null) {
            addOptions(model);
            model.addAttribute("error", error);
            model.addAttribute("loginId", loginId);
            model.addAttribute("name", name);
            model.addAttribute("department", department);
            model.addAttribute("campus", campus);
            model.addAttribute("grade", grade);
            model.addAttribute("birthDate", birthDate);
            return "admin/student-form";
        }
        ra.addFlashAttribute("success", name.trim() + "(" + loginId.trim()
                + ") 학생을 등록했습니다. 초기 비밀번호는 생년월일 6자리입니다.");
        return "redirect:/admin/students?q=" + loginId.trim();
    }

    @PostMapping("/admin/students/{id}/unlock")
    public String unlock(@PathVariable Long id, @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page, RedirectAttributes ra) {
        Student s = studentService.unlock(id);
        ra.addFlashAttribute("success", s.getName() + "(" + s.getLoginId() + ") 계정의 잠금을 해제했습니다.");
        return backToList(ra, q, page);
    }

    @PostMapping("/admin/students/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam(defaultValue = "") String q,
                                @RequestParam(defaultValue = "0") int page, RedirectAttributes ra) {
        String error = studentService.resetByAdmin(id);
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("success", "비밀번호를 생년월일 6자리로 초기화했습니다. 다음 로그인 시 변경해야 합니다.");
        }
        return backToList(ra, q, page);
    }

    private String backToList(RedirectAttributes ra, String q, int page) {
        ra.addAttribute("q", q);
        ra.addAttribute("page", page);
        return "redirect:/admin/students";
    }

    private void addOptions(Model model) {
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("campuses", CAMPUSES);
        model.addAttribute("grades", GRADES);
    }
}
