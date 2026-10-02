package com.donga.qlhotro.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.RequestAssignmentDTO;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.RequestAssignmentService;

@RestController
@RequestMapping("/api/technician")
public class TechnicianRequestApiController {

    private final RequestAssignmentService requestAssignmentService;
    private final UserRepository userRepository;

    public TechnicianRequestApiController(RequestAssignmentService requestAssignmentService, UserRepository userRepository) {
        this.requestAssignmentService = requestAssignmentService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RequestAssignmentDTO>>> getAssignedRequests(
            @RequestParam(required = false) Long technicianId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);

        Long effectiveTechId;
        if (SecurityUtils.isAdmin() && technicianId != null) {
            effectiveTechId = technicianId;
        } else {
            effectiveTechId = currentUser.getId();
        }

        List<RequestAssignmentDTO> assignments = requestAssignmentService.getAssignmentsByTechnician(effectiveTechId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu được phân công thành công.", assignments));
    }
}