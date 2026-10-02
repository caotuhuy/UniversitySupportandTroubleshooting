package com.donga.qlhotro.api;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.FeedbackCreateDTO;
import com.donga.qlhotro.dto.FeedbackResponseDTO;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class FeedbackApiController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public FeedbackApiController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/requests/{id}/feedback
     * Tạo đánh giá cho yêu cầu đã hoàn thành.
     */
    @PostMapping("/requests/{id}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> createFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackCreateDTO requestDTO) {
        requestDTO.setRequestId(id);

        // Bảo mật Ownership: Nếu có phiên đăng nhập, dùng ID của tài khoản đang đăng nhập
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent()) {
            requestDTO.setUserId(currentUser.get().getId());
        }

        FeedbackResponseDTO feedback = feedbackService.createFeedback(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đánh giá thành công.", feedback));
    }

    /**
     * GET /api/requests/{id}/feedback
     * Lấy đánh giá phản hồi của một yêu cầu cụ thể.
     */
    @GetMapping("/requests/{id}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> getFeedbackByRequest(@PathVariable Long id) {
        try {
            FeedbackResponseDTO feedback = feedbackService.getFeedbackByRequest(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công.", feedback));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.ok(ApiResponse.success("Chưa có đánh giá cho yêu cầu này.", null));
        }
    }

    /**
     * GET /api/feedbacks/user/{userId}
     * Lấy danh sách đánh giá của một người dùng.
     */
    @GetMapping("/feedbacks/user/{userId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getFeedbackByUser(@PathVariable Long userId) {
        // Nếu là sinh viên, chỉ được xem feedback của chính mình
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !currentUser.get().getId().equals(userId)) {
            userId = currentUser.get().getId();
        }

        List<FeedbackResponseDTO> feedbacks = feedbackService.getFeedbackByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công.", feedbacks));
    }

    /**
     * GET /api/feedbacks
     * Dành cho Admin xem toàn bộ đánh giá của hệ thống.
     */
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getAllFeedbacks() {
        List<FeedbackResponseDTO> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách đánh giá thành công.", feedbacks));
    }

    /**
     * DELETE /api/feedbacks/{id}
     * Dành cho Admin xóa đánh giá không phù hợp hoặc theo yêu cầu quản trị.
     */
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.DeleteMapping("/feedbacks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công.", null));
    }
}