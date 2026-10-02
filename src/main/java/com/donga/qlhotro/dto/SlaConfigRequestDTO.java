package com.donga.qlhotro.dto;

import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.SlaStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SlaConfigRequestDTO {

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    private Priority priority;

    @NotNull(message = "Thời gian phản hồi không được để trống")
    @Min(value = 1, message = "Thời gian phản hồi phải lớn hơn 0")
    private Integer responseTime;

    @NotNull(message = "Thời gian xử lý không được để trống")
    @Min(value = 1, message = "Thời gian xử lý phải lớn hơn 0")
    private Integer resolutionTime;

    @NotNull(message = "Thời gian cảnh báo không được để trống")
    @Min(value = 1, message = "Thời gian cảnh báo phải lớn hơn 0")
    private Integer warningTime;

    private SlaStatus status;

    public SlaConfigRequestDTO() {
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

    public SlaStatus getStatus() {
        return status;
    }

    public void setStatus(SlaStatus status) {
        this.status = status;
    }

    // Aliases for compatibility with frontend payloads
    public Integer getResponseTimeHours() {
        return responseTime;
    }

    public void setResponseTimeHours(Integer responseTimeHours) {
        this.responseTime = responseTimeHours;
    }

    public Integer getResolutionTimeHours() {
        return resolutionTime;
    }

    public void setResolutionTimeHours(Integer resolutionTimeHours) {
        this.resolutionTime = resolutionTimeHours;
    }

    public Integer getWarningTimeHours() {
        return warningTime;
    }

    public void setWarningTimeHours(Integer warningTimeHours) {
        this.warningTime = warningTimeHours;
    }
}
