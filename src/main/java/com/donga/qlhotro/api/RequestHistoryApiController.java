package com.donga.qlhotro.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.RequestHistoryResponseDTO;
import com.donga.qlhotro.service.RequestHistoryService;
import com.donga.qlhotro.service.SupportRequestService;

@RestController
@RequestMapping("/api/requests")
public class RequestHistoryApiController {

    private final RequestHistoryService requestHistoryService;
    private final SupportRequestService supportRequestService;

    public RequestHistoryApiController(RequestHistoryService requestHistoryService,
                                       SupportRequestService supportRequestService) {
        this.requestHistoryService = requestHistoryService;
        this.supportRequestService = supportRequestService;
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<RequestHistoryResponseDTO>>> getHistory(@PathVariable Long id) {
        supportRequestService.getRequestById(id);
        List<RequestHistoryResponseDTO> history = requestHistoryService.getHistoryByRequestId(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử xử lý thành công.", history));
    }
}