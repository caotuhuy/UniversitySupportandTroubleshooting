package com.donga.qlhotro.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.NotificationResponseDTO;
import com.donga.qlhotro.security.CustomUserDetails;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.NotificationService;

import com.donga.qlhotro.entity.Notification;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.NotificationRepository;
import com.donga.qlhotro.repository.UserRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationApiController(NotificationService notificationService,
                                      NotificationRepository notificationRepository,
                                      UserRepository userRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponseDTO>>> getByUser(@PathVariable Long userId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        if (!SecurityUtils.isAdmin() && !currentUser.getId().equals(userId)) {
            userId = currentUser.getId(); // Bắt buộc chỉ xem thông báo của chính mình
        }
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công.", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> markAsRead(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", id));
        if (!SecurityUtils.isAdmin() && (notification.getUser() == null || !notification.getUser().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên thông báo này.");
        }
        NotificationResponseDTO result = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu thông báo đã đọc thành công.", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", id));
        if (!SecurityUtils.isAdmin() && (notification.getUser() == null || !notification.getUser().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Bạn không có quyền xóa thông báo này.");
        }
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thông báo thành công.", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponseDTO>> createNotification(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String title = body.get("title").toString();
        String content = body.get("content").toString();
        String type = body.getOrDefault("type", "SYSTEM").toString();
        NotificationResponseDTO created = notificationService.createNotification(userId, title, content, type);
        return ResponseEntity.ok(ApiResponse.success("Tạo thông báo thành công.", created));
    }
}