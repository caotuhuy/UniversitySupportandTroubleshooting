package com.donga.qlhotro.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor tự động inject "currentUri" vào Model của mọi request.
 * Dùng để thay thế #request.requestURI trong Thymeleaf 3.1+ (không còn
 * hỗ trợ các biến #request, #session, #response trong template expression).
 */
public class CurrentUriInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView != null) {
            modelAndView.addObject("currentUri", request.getRequestURI());
        }
    }
}
