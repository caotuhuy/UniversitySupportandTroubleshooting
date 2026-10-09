package com.donga.qlhotro.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donga.qlhotro.dto.FeedbackCreateDTO;
import com.donga.qlhotro.dto.FeedbackResponseDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.entity.Feedback;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.RequestStatus;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.FeedbackRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                               SupportRequestRepository supportRequestRepository,
                               UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(FeedbackCreateDTO requestDTO) {
        if (requestDTO.getUserId() == null) {
            throw new IllegalArgumentException("ID người đánh giá không được để trống");
        }

        if (requestDTO.getRating() == null || requestDTO.getRating() < 1 || requestDTO.getRating() > 5) {
            throw new IllegalArgumentException("Đánh giá rating phải từ 1 đến 5 sao");
        }

        SupportRequest request = supportRequestRepository.findById(requestDTO.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestDTO.getRequestId()));

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalArgumentException("Chỉ có thể đánh giá yêu cầu đã hoàn thành");
        }

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", requestDTO.getUserId()));

        if (request.getRequester() == null || !request.getRequester().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Chỉ người gửi yêu cầu mới được đánh giá");
        }

        if (feedbackRepository.findByRequestIdAndUserId(request.getId(), user.getId()).isPresent()) {
            throw new IllegalArgumentException("Mỗi người dùng chỉ được đánh giá một lần cho cùng một yêu cầu");
        }

        Feedback feedback = new Feedback();
        feedback.setRequest(request);
        feedback.setUser(user);
        feedback.setRating(requestDTO.getRating());
        feedback.setComment(requestDTO.getComment());

        Feedback saved = feedbackRepository.save(feedback);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponseDTO getFeedbackByRequest(Long requestId) {
        Feedback feedback = feedbackRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá phản hồi", "requestId", requestId));
        return mapToDTO(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getFeedbackByUser(Long userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getAllFeedbacks() {
        return feedbackRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedbackResponseDTO updateFeedback(Long id, FeedbackCreateDTO requestDTO) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá phản hồi", "id", id));

        if (requestDTO.getRating() != null) {
            if (requestDTO.getRating() < 1 || requestDTO.getRating() > 5) {
                throw new IllegalArgumentException("Đánh giá rating phải từ 1 đến 5 sao");
            }
            feedback.setRating(requestDTO.getRating());
        }

        if (requestDTO.getComment() != null) {
            feedback.setComment(requestDTO.getComment());
        }

        Feedback updated = feedbackRepository.save(feedback);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new ResourceNotFoundException("Đánh giá phản hồi", "id", id);
        }
        feedbackRepository.deleteById(id);
    }

    private FeedbackResponseDTO mapToDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        if (feedback.getRequest() != null) {
            dto.setRequestId(feedback.getRequest().getId());
            dto.setRequestCode(feedback.getRequest().getRequestCode());
            dto.setRequestTitle(feedback.getRequest().getTitle());
        }
        dto.setRating(feedback.getRating());
        dto.setComment(feedback.getComment());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());

        if (feedback.getUser() != null) {
            User u = feedback.getUser();
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
