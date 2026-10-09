package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDTO> getNotificationsByUser(Long userId);
    NotificationResponseDTO createNotification(Long userId, String title, String content, String type);
    NotificationResponseDTO markAsRead(Long id);
    void deleteNotification(Long id);
}
