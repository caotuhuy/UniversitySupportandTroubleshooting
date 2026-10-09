package com.donga.qlhotro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "profile";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/requests")
    public String requests() {
        return "requests";
    }

    @GetMapping("/create-request")
    public String createRequest() {
        return "create-request";
    }

    @GetMapping("/requests/{id}")
    public String requestDetail() {
        return "request-detail";
    }

    @GetMapping("/requests/{id}/edit")
    public String requestEdit() {
        return "request-edit";
    }

    @GetMapping("/technician/requests")
    public String technicianRequests() {
        return "technician-requests";
    }

    @GetMapping("/admin/requests")
    public String adminRequests() {
        return "admin-requests";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin-users";
    }

    @GetMapping("/admin/categories")
    public String adminCategories() {
        return "admin-categories";
    }

    @GetMapping("/admin/rooms")
    public String adminRooms() {
        return "admin-rooms";
    }

    @GetMapping("/admin/devices")
    public String adminDevices() {
        return "admin-devices";
    }

    @GetMapping("/admin/sla")
    public String adminSla() {
        return "admin-sla";
    }

    @GetMapping("/admin/feedbacks")
    public String adminFeedbacks() {
        return "admin-feedbacks";
    }
}
