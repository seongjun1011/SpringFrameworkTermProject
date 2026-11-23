package com.kopo.portal.config;

import com.kopo.portal.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 로그인 여부를 확인하는 인터셉터.
 * 1) 세션에 로그인한 학생 정보가 없으면 로그인 페이지로 보낸다.
 * 2) 초기 비밀번호를 아직 바꾸지 않은 학생은 비밀번호 변경 화면과 로그아웃만 이용할 수 있다.
 * (Spring Security를 아직 배우지 않은 단계라, HttpSession 기반으로 직접 구현했다.)
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final String CHANGE_PATH = "/password/change";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Student student = session == null ? null : (Student) session.getAttribute("loginStudent");
        if (student == null) {
            response.sendRedirect("/login");
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (student.isPasswordChangeRequired()
                && !path.equals(CHANGE_PATH) && !path.equals("/logout")) {
            response.sendRedirect(CHANGE_PATH);
            return false;
        }
        return true;
    }
}
