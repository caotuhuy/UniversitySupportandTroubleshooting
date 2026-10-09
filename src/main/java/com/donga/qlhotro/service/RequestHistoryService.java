package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.RequestHistoryResponseDTO;

import java.util.List;

public interface RequestHistoryService {
    List<RequestHistoryResponseDTO> getHistoryByRequestId(Long requestId);
    RequestHistoryResponseDTO createHistory(Long requestId, Long userId, String action, String oldStatus, String newStatus, String note);
}
