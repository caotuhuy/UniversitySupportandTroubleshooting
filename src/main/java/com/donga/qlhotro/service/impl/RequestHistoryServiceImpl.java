package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.RequestHistoryResponseDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.entity.RequestHistory;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RequestHistoryRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.service.RequestHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestHistoryServiceImpl implements RequestHistoryService {

    private final RequestHistoryRepository requestHistoryRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;

    public RequestHistoryServiceImpl(RequestHistoryRepository requestHistoryRepository,
                                     SupportRequestRepository supportRequestRepository,
                                     UserRepository userRepository) {
        this.requestHistoryRepository = requestHistoryRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestHistoryResponseDTO> getHistoryByRequestId(Long requestId) {
        return requestHistoryRepository.findByRequestIdOrderByCreatedAtDesc(requestId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestHistoryResponseDTO createHistory(Long requestId, Long userId, String action, String oldStatus, String newStatus, String note) {
        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        RequestHistory history = new RequestHistory();
        history.setRequest(request);
        history.setUser(user);
        history.setAction(action);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);

        RequestHistory saved = requestHistoryRepository.save(history);
        return mapToDTO(saved);
    }

    private RequestHistoryResponseDTO mapToDTO(RequestHistory history) {
        RequestHistoryResponseDTO dto = new RequestHistoryResponseDTO();
        dto.setId(history.getId());
        if (history.getRequest() != null) {
            dto.setRequestId(history.getRequest().getId());
        }
        dto.setAction(history.getAction());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setNote(history.getNote());
        dto.setCreatedAt(history.getCreatedAt());

        if (history.getUser() != null) {
            User u = history.getUser();
            UserResponseDTO uDto = new UserResponseDTO();
            uDto.setId(u.getId());
            uDto.setUsername(u.getUsername());
            uDto.setFullName(u.getFullName());
            uDto.setEmail(u.getEmail());
            uDto.setPhone(u.getPhone());
            uDto.setStudentCode(u.getStudentCode());
            uDto.setEmployeeCode(u.getEmployeeCode());
            uDto.setStatus(u.getStatus());
            dto.setUser(uDto);
        }

        return dto;
    }
}
