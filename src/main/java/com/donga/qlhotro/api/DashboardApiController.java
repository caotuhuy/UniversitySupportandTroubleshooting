package com.donga.qlhotro.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.DashboardStatisticsDTO;
import com.donga.qlhotro.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsDTO>> getStatistics() {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê dashboard thành công.",
                dashboardService.getStatistics()));
    }
}