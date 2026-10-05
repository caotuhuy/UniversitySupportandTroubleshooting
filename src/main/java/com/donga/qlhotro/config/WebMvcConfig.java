package com.donga.qlhotro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Spring MVC: đăng ký CurrentUriInterceptor để inject
 * biến "currentUri" vào Model của mọi trang Thymeleaf.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CurrentUriInterceptor())
                // Áp dụng cho mọi URL, trừ static resources
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }
}
