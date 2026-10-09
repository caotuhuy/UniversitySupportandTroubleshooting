package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.FeedbackCreateDTO;
import com.donga.qlhotro.dto.FeedbackResponseDTO;

import java.util.List;

public interface FeedbackService {
    FeedbackResponseDTO createFeedback(FeedbackCreateDTO request);
    FeedbackResponseDTO getFeedbackByRequest(Long requestId);
    List<FeedbackResponseDTO> getFeedbackByUser(Long userId);
    List<FeedbackResponseDTO> getAllFeedbacks();
    FeedbackResponseDTO updateFeedback(Long id, FeedbackCreateDTO request);
    void deleteFeedback(Long id);
}
