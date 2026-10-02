package com.donga.qlhotro.dto;

import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.SlaStatus;

import java.time.LocalDateTime;

public class SlaConfigResponseDTO {

    private Long id;
    private Priority priority;
    private Integer responseTime;
    private Integer resolutionTime;
    private Integer warningTime;
    private SlaStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SlaConfigResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public Integer getResolutionTime() {
        return resolutionTime;
    }

    public void setResolutionTime(Integer resolutionTime) {
        this.resolutionTime = resolutionTime;
    }

    public Integer getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(Integer warningTime) {
        this.warningTime = warningTime;
    }

    public Integer getResponseTimeHours() {
        return responseTime;
    }

    public Integer getResolutionTimeHours() {
        return resolutionTime;
    }

    public Integer getWarningTimeHours() {
        return warningTime;
    }

    public SlaStatus getStatus() {
        return status;
    }

    public void setStatus(SlaStatus status) {
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
}
