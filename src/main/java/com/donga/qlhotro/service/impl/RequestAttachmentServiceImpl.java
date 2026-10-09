package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.RequestAttachmentResponseDTO;
import com.donga.qlhotro.entity.RequestAttachment;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RequestAttachmentRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.RequestAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RequestAttachmentServiceImpl implements RequestAttachmentService {

    private final RequestAttachmentRepository attachmentRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public RequestAttachmentServiceImpl(
            RequestAttachmentRepository attachmentRepository,
            SupportRequestRepository supportRequestRepository,
            UserRepository userRepository) {
        this.attachmentRepository = attachmentRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
        this.uploadDir = Paths.get("uploads", "attachments").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file đính kèm: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RequestAttachmentResponseDTO uploadAttachment(Long requestId, Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File đính kèm không được rỗng.");
        }

        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Tên file không hợp lệ: " + originalFilename);
        }

        String storedFileName = UUID.randomUUID() + "_" + originalFilename;
        Path targetLocation = this.uploadDir.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi lưu file: " + ex.getMessage(), ex);
        }

        RequestAttachment attachment = new RequestAttachment();
        attachment.setRequest(request);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(targetLocation.toString());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(user);
        attachment.setUploadedAt(LocalDateTime.now());

        RequestAttachment saved = attachmentRepository.save(attachment);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestAttachmentResponseDTO> getAttachmentsByRequestId(Long requestId) {
        if (!supportRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestId);
        }
        return attachmentRepository.findByRequestId(requestId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAttachmentAsResource(Long attachmentId) {
        RequestAttachment attachment = getAttachmentEntity(attachmentId);
        try {
            Path filePath = Paths.get(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File đính kèm không tồn tại trên hệ thống", "id", attachmentId);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Đường dẫn file đính kèm không hợp lệ", "id", attachmentId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RequestAttachment getAttachmentEntity(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("File đính kèm", "id", attachmentId));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long userId) {
        RequestAttachment attachment = getAttachmentEntity(attachmentId);

        boolean isAdmin = SecurityUtils.isAdmin();
        boolean isOwner = attachment.getUploadedBy() != null && attachment.getUploadedBy().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền xóa tệp đính kèm này.");
        }

        try {
            Path filePath = Paths.get(attachment.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }

        attachmentRepository.delete(attachment);
    }

    private RequestAttachmentResponseDTO mapToDTO(RequestAttachment attachment) {
        RequestAttachmentResponseDTO dto = new RequestAttachmentResponseDTO();
        dto.setId(attachment.getId());
        if (attachment.getRequest() != null) {
            dto.setRequestId(attachment.getRequest().getId());
        }
        dto.setFileName(attachment.getFileName());
        dto.setFilePath(attachment.getFilePath());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        if (attachment.getUploadedBy() != null) {
            dto.setUploadedById(attachment.getUploadedBy().getId());
            dto.setUploadedByName(attachment.getUploadedBy().getFullName());
        }
        dto.setUploadedAt(attachment.getUploadedAt());
        if (attachment.getRequest() != null && attachment.getId() != null) {
            dto.setDownloadUrl("/api/requests/" + attachment.getRequest().getId() + "/attachments/" + attachment.getId() + "/download");
        }
        return dto;
    }
}
