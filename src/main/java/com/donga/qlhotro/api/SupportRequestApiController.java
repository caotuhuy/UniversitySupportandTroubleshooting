package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.SupportRequestCreateDTO;
import com.donga.qlhotro.dto.SupportRequestResponseDTO;
import com.donga.qlhotro.dto.SupportRequestUpdateDTO;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;
import com.donga.qlhotro.service.SupportRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;

@RestController
@RequestMapping("/api/requests")
public class SupportRequestApiController {

    private final SupportRequestService supportRequestService;
    private final UserRepository userRepository;

    public SupportRequestApiController(SupportRequestService supportRequestService, UserRepository userRepository) {
        this.supportRequestService = supportRequestService;
        this.userRepository = userRepository;
    }

    // =====================================================================
    // CRUD CƠ BẢN
    // =====================================================================

    /**
     * GET /api/requests
     * Lấy toàn bộ danh sách yêu cầu hỗ trợ.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportRequestResponseDTO>>> getAllRequests() {
        List<SupportRequestResponseDTO> list = supportRequestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu hỗ trợ thành công.", list));
    }

    /**
     * GET /api/requests/{id}
     * Lấy chi tiết một yêu cầu hỗ trợ theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> getRequestById(@PathVariable Long id) {
        SupportRequestResponseDTO dto = supportRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy yêu cầu hỗ trợ thành công.", dto));
    }

    /**
     * POST /api/requests
     * Tạo yêu cầu hỗ trợ mới.
     * Body: SupportRequestCreateDTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> createRequest(
            @Valid @RequestBody SupportRequestCreateDTO requestDTO) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        if (SecurityUtils.isStudent()) {
            requestDTO.setRequesterId(currentUser.getId());
        } else if (requestDTO.getRequesterId() == null) {
            requestDTO.setRequesterId(currentUser.getId());
        }
        SupportRequestResponseDTO created = supportRequestService.createRequest(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu hỗ trợ thành công.", created));
    }

    /**
     * PUT /api/requests/{id}
     * Cập nhật thông tin yêu cầu hỗ trợ.
     * Body: SupportRequestUpdateDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody SupportRequestUpdateDTO requestDTO) {
        SupportRequestResponseDTO updated = supportRequestService.updateRequest(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật yêu cầu hỗ trợ thành công.", updated));
    }

    /**
     * DELETE /api/requests/{id}
     * Xóa yêu cầu hỗ trợ.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        supportRequestService.deleteRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa yêu cầu hỗ trợ thành công.", null));
    }

    // =====================================================================
    // TÌM KIẾM & LỌC
    // =====================================================================

    /**
     * GET /api/requests/search?keyword=...
     * Tìm kiếm yêu cầu theo từ khóa (requestCode, title, description).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupportRequestResponseDTO>>> searchRequests(
            @RequestParam String keyword) {
        List<SupportRequestResponseDTO> results = supportRequestService.searchRequests(keyword);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm yêu cầu hỗ trợ thành công.", results));
    }

    /**
     * GET /api/requests/filter/status?status=PENDING
     * Lọc yêu cầu theo trạng thái.
     */
    @GetMapping("/filter/status")
    public ResponseEntity<ApiResponse<List<SupportRequestResponseDTO>>> filterByStatus(
            @RequestParam RequestStatus status) {
        List<SupportRequestResponseDTO> results = supportRequestService.filterByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Lọc yêu cầu theo trạng thái thành công.", results));
    }

    /**
     * GET /api/requests/filter/priority?priority=HIGH
     * Lọc yêu cầu theo mức độ ưu tiên.
     */
    @GetMapping("/filter/priority")
    public ResponseEntity<ApiResponse<List<SupportRequestResponseDTO>>> filterByPriority(
            @RequestParam Priority priority) {
        List<SupportRequestResponseDTO> results = supportRequestService.filterByPriority(priority);
        return ResponseEntity.ok(ApiResponse.success("Lọc yêu cầu theo độ ưu tiên thành công.", results));
    }

    /**
     * GET /api/requests/filter/requester?requesterId=1
     * Lọc yêu cầu theo người gửi.
     */
    @GetMapping("/filter/requester")
    public ResponseEntity<ApiResponse<List<SupportRequestResponseDTO>>> filterByRequester(
            @RequestParam Long requesterId) {
        List<SupportRequestResponseDTO> results = supportRequestService.filterByRequester(requesterId);
        return ResponseEntity.ok(ApiResponse.success("Lọc yêu cầu theo người gửi thành công.", results));
    }

    // =====================================================================
    // THAO TÁC NGHIỆP VỤ (BUSINESS ACTIONS)
    // =====================================================================

    /**
     * POST /api/requests/{id}/cancel
     * Hủy yêu cầu hỗ trợ.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> cancelRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.cancelRequest(id, currentUser.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success("Hủy yêu cầu hỗ trợ thành công.", result));
    }

    /**
     * POST /api/requests/{id}/receive
     * Tiếp nhận yêu cầu hỗ trợ.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> receiveRequest(
            @PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        SupportRequestResponseDTO result = supportRequestService.receiveRequest(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Tiếp nhận yêu cầu hỗ trợ thành công.", result));
    }

    /**
     * POST /api/requests/{id}/reject
     * Từ chối yêu cầu hỗ trợ.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.rejectRequest(id, currentUser.getId(), reason);
        return ResponseEntity.ok(ApiResponse.success("Từ chối yêu cầu hỗ trợ thành công.", result));
    }

    /**
     * POST /api/requests/{id}/assign
     * Phân công kỹ thuật viên xử lý yêu cầu.
     * Body: { "technicianId": 2, "note": "..." }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> assignRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        if (!body.containsKey("technicianId") || body.get("technicianId") == null) {
            throw new IllegalArgumentException("Vui lòng chọn kỹ thuật viên để phân công.");
        }
        Long technicianId = Long.valueOf(body.get("technicianId").toString());
        String note = body.getOrDefault("note", "").toString();
        SupportRequestResponseDTO result = supportRequestService.assignRequest(id, technicianId, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Phân công xử lý yêu cầu thành công.", result));
    }

    /**
     * POST /api/requests/{id}/reassign
     * Tái phân công kỹ thuật viên xử lý yêu cầu.
     * Body: { "newTechnicianId": 3, "note": "..." }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reassign")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> reassignRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        if (!body.containsKey("newTechnicianId") || body.get("newTechnicianId") == null) {
            throw new IllegalArgumentException("Vui lòng chọn kỹ thuật viên mới.");
        }
        Long newTechnicianId = Long.valueOf(body.get("newTechnicianId").toString());
        String note = body.getOrDefault("note", "").toString();
        SupportRequestResponseDTO result = supportRequestService.reassignRequest(id, newTechnicianId, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Tái phân công kỹ thuật viên thành công.", result));
    }

    /**
     * POST /api/requests/{id}/accept
     * Kỹ thuật viên chấp nhận xử lý yêu cầu.
     */
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> acceptRequest(
            @PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        SupportRequestResponseDTO result = supportRequestService.acceptRequest(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận xử lý yêu cầu thành công.", result));
    }

    /**
     * POST /api/requests/{id}/progress
     * Cập nhật tiến độ xử lý.
     * Body: { "note": "..." }
     */
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> updateProgress(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String note = body != null && body.get("note") != null ? body.get("note").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.updateProgress(id, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến độ xử lý thành công.", result));
    }

    /**
     * POST /api/requests/{id}/need-info
     * Yêu cầu thêm thông tin từ người gửi.
     * Body: { "note": "..." }
     */
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/{id}/need-info")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> requestMoreInformation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String note = body != null && body.get("note") != null ? body.get("note").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.requestMoreInformation(id, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Đã yêu cầu thêm thông tin thành công.", result));
    }

    /**
     * POST /api/requests/{id}/complete
     * Hoàn thành yêu cầu hỗ trợ.
     * Body: { "note": "..." }
     */
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> completeRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String note = body != null && body.get("note") != null ? body.get("note").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.completeRequest(id, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Hoàn thành yêu cầu hỗ trợ thành công.", result));
    }

    /**
     * PATCH /api/requests/{id}/priority
     * Cập nhật mức độ ưu tiên của yêu cầu.
     * Body: { "priority": "HIGH" }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/priority")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> updatePriority(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Priority priority = Priority.valueOf(body.get("priority").toString());
        SupportRequestResponseDTO result = supportRequestService.updatePriority(id, priority);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật độ ưu tiên thành công.", result));
    }

    /**
     * PATCH /api/requests/{id}/category
     * Thay đổi danh mục của yêu cầu.
     * Body: { "categoryId": 2 }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/category")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long categoryId = Long.valueOf(body.get("categoryId").toString());
        SupportRequestResponseDTO result = supportRequestService.updateCategory(id, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục yêu cầu thành công.", result));
    }

    /**
     * POST /api/requests/{id}/resume
     * Tiếp tục xử lý sau khi người dùng cung cấp thêm thông tin.
     * Chuyển trạng thái: WAITING_INFO → IN_PROGRESS
     * Body: { "note": "..." }
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<SupportRequestResponseDTO>> resumeRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        String note = body != null && body.get("note") != null ? body.get("note").toString() : "";
        SupportRequestResponseDTO result = supportRequestService.resumeRequest(id, currentUser.getId(), note);
        return ResponseEntity.ok(ApiResponse.success("Tiếp tục xử lý yêu cầu thành công.", result));
    }
}

