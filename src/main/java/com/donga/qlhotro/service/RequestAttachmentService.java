package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.RequestAttachmentResponseDTO;
import com.donga.qlhotro.entity.RequestAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RequestAttachmentService {
    RequestAttachmentResponseDTO uploadAttachment(Long requestId, Long userId, MultipartFile file);
    List<RequestAttachmentResponseDTO> getAttachmentsByRequestId(Long requestId);
    Resource loadAttachmentAsResource(Long attachmentId);
    RequestAttachment getAttachmentEntity(Long attachmentId);
    void deleteAttachment(Long attachmentId, Long userId);
}
