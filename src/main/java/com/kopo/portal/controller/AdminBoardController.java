package com.kopo.portal.controller;

import com.kopo.portal.model.Staff;
import com.kopo.portal.service.NoticeService;
import com.kopo.portal.service.ScheduleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * 학사관리자 > 게시판 관리: 공지사항 작성, 학사일정 등록.
 * 작성한 공지·일정은 학생 화면(공지사항, 학사일정, 대시보드)에 바로 나타난다.
 */
@Controller
public class AdminBoardController {

    private final NoticeService noticeService;
    private final ScheduleService scheduleService;

    public AdminBoardController(NoticeService noticeService, ScheduleService scheduleService) {
        this.noticeService = noticeService;
        this.scheduleService = scheduleService;
    }

    // ===== 공지사항 =====

    @GetMapping({"/admin/notices", "/admin/notices/new"})
    public String notices(Model model) {
        model.addAttribute("notices", noticeService.findAll());
        return "admin/notices";
    }

    @PostMapping("/admin/notices/new")
    public String writeNotice(@RequestParam String title, @RequestParam String content,
                              HttpSession session, Model model, RedirectAttributes ra) {
        Staff staff = (Staff) session.getAttribute("loginStaff");
        String error = noticeService.write(staff, title, content);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            return notices(model);
        }
        ra.addFlashAttribute("success", "공지사항을 등록했습니다.");
        return "redirect:/admin/notices";
    }

    // ===== 학사일정 =====

    @GetMapping({"/admin/schedules", "/admin/schedules/new"})
    public String schedules(Model model) {
        model.addAttribute("schedules", scheduleService.findAllLatestFirst());
        return "admin/schedules";
    }

    @PostMapping("/admin/schedules/new")
    public String createSchedule(@RequestParam String title,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 Model model, RedirectAttributes ra) {
        String error = scheduleService.create(title, startDate, endDate);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("title", title);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            return schedules(model);
        }
        ra.addFlashAttribute("success", "학사일정을 등록했습니다.");
        return "redirect:/admin/schedules";
    }
}
