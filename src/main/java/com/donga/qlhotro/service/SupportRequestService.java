package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.SupportRequestCreateDTO;
import com.donga.qlhotro.dto.SupportRequestResponseDTO;
import com.donga.qlhotro.dto.SupportRequestUpdateDTO;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;

import java.util.List;

public interface SupportRequestService {
    List<SupportRequestResponseDTO> getAllRequests();
    SupportRequestResponseDTO getRequestById(Long id);
    SupportRequestResponseDTO createRequest(SupportRequestCreateDTO request);
    SupportRequestResponseDTO updateRequest(Long id, SupportRequestUpdateDTO request);
    void deleteRequest(Long id);
    SupportRequestResponseDTO cancelRequest(Long id, Long userId, String reason);
    SupportRequestResponseDTO receiveRequest(Long id, Long userId);
    SupportRequestResponseDTO rejectRequest(Long id, Long userId, String reason);
    SupportRequestResponseDTO assignRequest(Long id, Long technicianId, Long assignedById, String note);
    SupportRequestResponseDTO updatePriority(Long id, Priority priority);
    SupportRequestResponseDTO updateCategory(Long id, Long categoryId);
    SupportRequestResponseDTO acceptRequest(Long id, Long technicianId);
    SupportRequestResponseDTO updateProgress(Long id, Long userId, String note);
    SupportRequestResponseDTO requestMoreInformation(Long id, Long userId, String note);
    SupportRequestResponseDTO completeRequest(Long id, Long userId, String note);
    List<SupportRequestResponseDTO> searchRequests(String keyword);
    List<SupportRequestResponseDTO> filterByStatus(RequestStatus status);
    List<SupportRequestResponseDTO> filterByPriority(Priority priority);
    List<SupportRequestResponseDTO> filterByRequester(Long requesterId);
    SupportRequestResponseDTO resumeRequest(Long id, Long userId, String note);
    SupportRequestResponseDTO reassignRequest(Long id, Long newTechnicianId, Long assignedById, String note);
}

