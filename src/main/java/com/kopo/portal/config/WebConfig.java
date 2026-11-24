package com.kopo.portal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 인터셉터를 어떤 경로에 적용할지 등록하는 설정 클래스.
 * 학생 화면은 LoginInterceptor, 교직원(관리자) 화면(/admin/**)은 AdminInterceptor가 검사한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] STATIC_AND_PUBLIC = {
            "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/login", "/password/reset", "/admin", "/admin/**")
                .excludePathPatterns(STATIC_AND_PUBLIC);

        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin", "/admin/**")
                .excludePathPatterns("/admin/login");
    }
}
