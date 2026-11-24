package com.kopo.portal.config;

import com.kopo.portal.model.Staff;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * 교직원(관리자) 화면(/admin/**) 접근 검사.
 * 1) 교직원으로 로그인하지 않았으면 교직원 로그인 화면으로 보낸다.
 * 2) 학사관리 메뉴는 학사관리자만, 결재함은 결재자(담당직원·학과장·장학위원회·학생처장)만 사용할 수 있다.
 */
public class AdminInterceptor implements HandlerInterceptor {

    /** 학사관리자 전용 경로 */
    private static final List<String> ACADEMIC_ADMIN_PATHS =
            List.of("/admin/students", "/admin/courses", "/admin/notices", "/admin/schedules");
    /** 결재자 전용 경로 */
    private static final String APPROVAL_PATH = "/admin/approvals";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Staff staff = session == null ? null : (Staff) session.getAttribute("loginStaff");
        String ctx = request.getContextPath();
        if (staff == null) {
            response.sendRedirect(ctx + "/admin/login");
            return false;
        }
        String path = request.getRequestURI().substring(ctx.length());
        boolean allowed = true;
        if (startsWithAny(path, ACADEMIC_ADMIN_PATHS)) {
            allowed = staff.getRole().isAcademicAdmin();
        } else if (path.equals(APPROVAL_PATH) || path.startsWith(APPROVAL_PATH + "/")) {
            allowed = staff.getRole().isApprover();
        }
        if (!allowed) {
            response.sendRedirect(ctx + "/admin?denied");
            return false;
        }
        return true;
    }

    private static boolean startsWithAny(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
    }
}
