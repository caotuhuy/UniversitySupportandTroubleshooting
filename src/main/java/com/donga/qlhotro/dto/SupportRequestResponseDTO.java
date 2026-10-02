package com.donga.qlhotro.dto;

import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;

import java.time.LocalDateTime;

public class SupportRequestResponseDTO {

    private Long id;
    private String requestCode;
    private String title;
    private String description;
    private CategoryResponseDTO category;
    private UserResponseDTO requester;
    private RoomResponseDTO room;
    private DeviceResponseDTO device;
    private Priority priority;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime resolutionDeadline;
    private String slaStatus;
    private String slaStatusText;
    private Boolean isOverdue = false;
    private UserResponseDTO currentTechnician;

    public SupportRequestResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryResponseDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDTO category) {
        this.category = category;
    }

    public UserResponseDTO getRequester() {
        return requester;
    }

    public void setRequester(UserResponseDTO requester) {
        this.requester = requester;
    }

    public RoomResponseDTO getRoom() {
        return room;
    }

    public void setRoom(RoomResponseDTO room) {
        this.room = room;
    }

    public DeviceResponseDTO getDevice() {
        return device;
    }

    public void setDevice(DeviceResponseDTO device) {
        this.device = device;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getResolutionDeadline() {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(LocalDateTime resolutionDeadline) {
        this.resolutionDeadline = resolutionDeadline;
    }

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String slaStatus) {
        this.slaStatus = slaStatus;
    }

    public String getSlaStatusText() {
        return slaStatusText;
    }

    public void setSlaStatusText(String slaStatusText) {
        this.slaStatusText = slaStatusText;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    public UserResponseDTO getCurrentTechnician() {
        return currentTechnician;
    }

    public void setCurrentTechnician(UserResponseDTO currentTechnician) {
        this.currentTechnician = currentTechnician;
    }
}
