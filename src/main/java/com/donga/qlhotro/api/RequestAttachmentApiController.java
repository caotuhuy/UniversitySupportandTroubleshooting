package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.RequestAttachmentResponseDTO;
import com.donga.qlhotro.entity.RequestAttachment;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RequestAssignmentRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.RequestAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}/attachments")
public class RequestAttachmentApiController {

    private final RequestAttachmentService attachmentService;
    private final UserRepository userRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;

    public RequestAttachmentApiController(
            RequestAttachmentService attachmentService,
            UserRepository userRepository,
            SupportRequestRepository supportRequestRepository,
            RequestAssignmentRepository requestAssignmentRepository) {
        this.attachmentService = attachmentService;
        this.userRepository = userRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
    }

    private void checkRequestAccess(Long requestId, User currentUser) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestId));

        boolean isRequester = request.getRequester() != null && request.getRequester().getId().equals(currentUser.getId());
        if (isRequester) {
            return;
        }

        boolean isAssignedTech = requestAssignmentRepository.findByRequestId(requestId).stream()
                .anyMatch(a -> a.getTechnician() != null && a.getTechnician().getId().equals(currentUser.getId()));
        if (isAssignedTech) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền truy cập tệp đính kèm của yêu cầu này.");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RequestAttachmentResponseDTO>> uploadAttachment(
            @PathVariable Long requestId,
            @RequestParam("file") MultipartFile file) {

        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        checkRequestAccess(requestId, currentUser);

        RequestAttachmentResponseDTO dto = attachmentService.uploadAttachment(requestId, currentUser.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải lên tệp đính kèm thành công.", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestAttachmentResponseDTO>>> getAttachments(@PathVariable Long requestId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        checkRequestAccess(requestId, currentUser);

        List<RequestAttachmentResponseDTO> list = attachmentService.getAttachmentsByRequestId(requestId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tệp đính kèm thành công.", list));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long requestId,
            @PathVariable Long attachmentId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        checkRequestAccess(requestId, currentUser);

        RequestAttachment attachment = attachmentService.getAttachmentEntity(attachmentId);
        Resource resource = attachmentService.loadAttachmentAsResource(attachmentId);

        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String contentType = attachment.getFileType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long requestId,
            @PathVariable Long attachmentId) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow(userRepository);
        checkRequestAccess(requestId, currentUser);

        attachmentService.deleteAttachment(attachmentId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Xóa tệp đính kèm thành công.", null));
    }
}
