package com.kopo.portal.controller;

import com.kopo.portal.model.Course;
import com.kopo.portal.service.CourseService;
import com.kopo.portal.service.CourseService.CourseForm;
import com.kopo.portal.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 학사관리자 > 수업 관리: 강좌 목록, 강좌 개설·수정, 강좌별 수강신청 현황.
 * (AdminInterceptor가 학사관리자만 접근하도록 검사한다.)
 */
@Controller
public class AdminCourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public AdminCourseController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/admin/courses")
    public String list(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "admin/courses";
    }

    @GetMapping("/admin/courses/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CourseForm("", "", "월", 1, 2, "", 30));
        return formView(model, null);
    }

    @PostMapping("/admin/courses/new")
    public String create(@RequestParam String name, @RequestParam String professor,
                         @RequestParam String dayOfWeek,
                         @RequestParam(required = false) Integer startPeriod,
                         @RequestParam(required = false) Integer endPeriod,
                         @RequestParam String location,
                         @RequestParam(required = false) Integer capacity,
                         Model model, RedirectAttributes ra) {
        CourseForm form = new CourseForm(name, professor, dayOfWeek, startPeriod, endPeriod, location, capacity);
        String error = courseService.create(form);
        if (error != null) {
            model.addAttribute("form", form);
            model.addAttribute("error", error);
            return formView(model, null);
        }
        ra.addFlashAttribute("success", "'" + name.trim() + "' 강좌를 개설했습니다.");
        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/courses/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Course c = courseService.findById(id).orElseThrow();
        model.addAttribute("form", new CourseForm(c.getName(), c.getProfessor(), c.getDayOfWeek(),
                c.getStartPeriod(), c.getEndPeriod(), c.getLocation(), c.getCapacity()));
        return formView(model, c);
    }

    @PostMapping("/admin/courses/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String name, @RequestParam String professor,
                         @RequestParam String dayOfWeek,
                         @RequestParam(required = false) Integer startPeriod,
                         @RequestParam(required = false) Integer endPeriod,
                         @RequestParam String location,
                         @RequestParam(required = false) Integer capacity,
                         Model model, RedirectAttributes ra) {
        CourseForm form = new CourseForm(name, professor, dayOfWeek, startPeriod, endPeriod, location, capacity);
        String error = courseService.update(id, form);
        if (error != null) {
            model.addAttribute("form", form);
            model.addAttribute("error", error);
            return formView(model, courseService.findById(id).orElseThrow());
        }
        ra.addFlashAttribute("success", "'" + name.trim() + "' 강좌를 수정했습니다.");
        return "redirect:/admin/courses";
    }

    @GetMapping("/admin/courses/{id}/enrollments")
    public String enrollments(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id).orElseThrow());
        model.addAttribute("enrollments", enrollmentService.findByCourse(id));
        return "admin/course-enrollments";
    }

    private String formView(Model model, Course editing) {
        model.addAttribute("course", editing);   // null이면 개설, 아니면 수정
        model.addAttribute("days", CourseService.DAYS);
        model.addAttribute("maxPeriod", CourseService.MAX_PERIOD);
        return "admin/course-form";
    }
}
