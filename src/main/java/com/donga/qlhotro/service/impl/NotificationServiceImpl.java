package com.donga.qlhotro.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donga.qlhotro.dto.NotificationResponseDTO;
import com.donga.qlhotro.entity.Notification;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.NotificationRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByUser(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponseDTO createNotification(Long userId, String title, String content, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", id));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        return mapToDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Thông báo", "id", id);
        }
        notificationRepository.deleteById(id);
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
        }
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}
